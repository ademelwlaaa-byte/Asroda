package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SensitiveTriggerEntity
import com.example.data.repository.BaselineEmotionEngine
import com.example.ui.animation.CosmicOrbLoader
import com.example.ui.animation.cosmicShimmerSweep
import com.example.ui.components.MorCamCircleIconButton
import com.example.ui.components.MorCamGlassSlider
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LiterarySerifFontFamily

internal val WIZARD_REL_TYPE_LABELS = mapOf(
    "parent" to "Ebeveyn",
    "sibling" to "Kardeş",
    "friend" to "Arkadaş",
    "romantic" to "Sevgili",
    "ex_romantic" to "Eski Sevgili",
    "conflicted_romantic" to "Çatışmalı Sevgili",
    "rival" to "Rakip",
    "enemy" to "Düşman",
    "stranger" to "Yabancı",
    "authority" to "Otorite",
    "mentor" to "Mentor",
    "subordinate" to "Ast",
    "companion_creature" to "Yaratık/Evcil",
    "other" to "Diğer"
)

@Composable
internal fun WizardBaselineReviewStep(
    draftBotId: String,
    mode: String,
    isAnalyzing: Boolean,
    analysisError: String?,
    analysisResult: BaselineEmotionEngine.BaselineAnalysisResult?,
    userEditedFields: Set<String>,
    reviewRelType: String,
    reviewAttachment: String,
    reviewSpeechStyle: String,
    reviewDominantEmotion: String,
    reviewRegulationCapacity: Int,
    reviewTriggers: List<SensitiveTriggerEntity>,
    reviewBigFive: Map<String, Int>,
    reviewAxes: Map<String, Int>,
    frostedBackdrop: ImageBitmap,
    scrollSync: Int,
    pulseGlowAlpha: Float,
    onRelTypeChange: (String) -> Unit,
    onAttachmentChange: (String) -> Unit,
    onDominantEmotionChange: (String) -> Unit,
    onSpeechStyleChange: (String) -> Unit,
    onRegulationCapacityChange: (Int) -> Unit,
    onAxisChange: (String, Int) -> Unit,
    onBigFiveChange: (String, Int) -> Unit,
    onTriggersChange: (List<SensitiveTriggerEntity>) -> Unit,
    onReanalyzeClick: () -> Unit,
    onResetToDefaultClick: () -> Unit
) {
    var showAdvancedPsychology by rememberSaveable { mutableStateOf(false) }

    WizardStepHeader(
        title = "Duygusal profil ve\nilişki analizi",
        subtitle = "Başlangıç duygu durumunu, ilişki eksenlerini ve hassas noktaları inceleyip özelleştirin.",
        glowAlpha = pulseGlowAlpha
    )

    Spacer(modifier = Modifier.height(18.dp))

    if (isAnalyzing) {
        WizardMorCamSectionCard(
            frostedBackdrop = frostedBackdrop,
            scrollSync = scrollSync,
            modifier = Modifier
                .cosmicShimmerSweep(active = true, cornerRadius = 22.dp)
                .testTag("wizard_baseline_loading")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CosmicOrbLoader(size = 44.dp)
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Yapay Zeka Karakterinizi Analiz Ediyor…",
                    fontFamily = LiterarySerifFontFamily,
                    color = Color(0xFFF3E8FF),
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Bağlanma stili, başlangıç eksenleri ve hassas tetikleyiciler hesaplanıyor.",
                    fontFamily = InterFontFamily,
                    color = Color(0xFFC2B2E4),
                    fontSize = 12.sp
                )
            }
        }
        return
    }

    if (analysisError != null) {
        Text(
            text = "⚠ $analysisError",
            fontFamily = InterFontFamily,
            color = Color(0xFFFCA5A5),
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        )
    }

    val res = analysisResult ?: return

    // 1. Top Psychological Summary Card (Source badge + confidence + relationshipNote + universe atmosphere)
    val isAiAnalyzed = res.source == BaselineEmotionEngine.BaselineSource.AI_ANALYZED
    val isUserEdited = userEditedFields.isNotEmpty()
    val (sourceBadge, sourceColor, showQuickRefresh) = when {
        isUserEdited -> Triple("✦ Kişiselleştirilmiş Profil", Color(0xFFFAF5FF), false)
        isAiAnalyzed -> Triple("✦ AI Psikoloji Profili", Color(0xFFE9D5FF), false)
        else -> Triple("⚡ Hızlı Yerel Profil — AI ile Yenile", Color(0xFFFDE68A), true)
    }

    WizardMorCamSectionCard(
        frostedBackdrop = frostedBackdrop,
        scrollSync = scrollSync,
        modifier = Modifier.testTag("wizard_baseline_summary_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (showQuickRefresh) Color(0x52451A03) else Color(0x5C3B1A74))
                    .border(1.dp, sourceColor.copy(alpha = 0.78f), RoundedCornerShape(50))
                    .then(
                        if (showQuickRefresh) Modifier.clickable { onReanalyzeClick() } else Modifier
                    )
                    .padding(horizontal = 12.dp, vertical = 5.dp)
                    .testTag("wizard_baseline_source_badge")
            ) {
                Text(
                    text = sourceBadge,
                    fontFamily = InterFontFamily,
                    color = sourceColor,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (res.confidence < 60) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0x52451A03))
                        .border(1.dp, Color(0xAAFB923C), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Güven: ${res.confidence}/100",
                        fontFamily = InterFontFamily,
                        color = Color(0xFFFDE047),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (res.relationshipNote.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = res.relationshipNote,
                fontFamily = LiterarySerifFontFamily,
                color = Color(0xFFF3E8FF),
                fontSize = 13.5.sp,
                lineHeight = 19.sp
            )
        }

        if (mode == "universe" && res.universeAtmosphere != null) {
            val ua = res.universeAtmosphere
            val summaryLine = listOfNotNull(
                ua.currentLocation.takeIf { it.isNotBlank() },
                ua.timeOfDay.takeIf { it.isNotBlank() },
                ua.sceneMood.takeIf { it.isNotBlank() }
            ).joinToString(" • ")
            if (summaryLine.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x402C1258))
                        .border(0.8.dp, Color(0x77C084FC), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFD8B4FE),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Evren Atmosferi: $summaryLine (Gerilim ${ua.sceneTension}/10)",
                        fontFamily = InterFontFamily,
                        color = Color(0xFFE5D8FF),
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    // 2. Relationship Type Selector
    WizardMorCamSectionCard(
        title = "İlişki Türü",
        subtitle = "Karakter ile aranızdaki başlangıç bağı",
        frostedBackdrop = frostedBackdrop,
        scrollSync = scrollSync
    ) {
        val relTypes = BaselineEmotionEngine.VALID_REL_TYPES.toList()
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.horizontalGradient(
                            0.00f to Color.Transparent,
                            0.05f to Color.White,
                            0.95f to Color.White,
                            1.00f to Color.Transparent
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
        ) {
            items(relTypes) { rt ->
                WizardMorCamPillChip(
                    label = WIZARD_REL_TYPE_LABELS[rt] ?: rt,
                    selected = reviewRelType == rt,
                    onClick = { onRelTypeChange(rt) }
                )
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    // 3. Initial Relationship Axes Sliders (Affection, Respect, Comfort, Resentment)
    WizardMorCamSectionCard(
        title = "Başlangıç İlişki Eksenleri",
        subtitle = "Sohbetin ilk mesajında karakterin sana karşı duygusal konumu",
        frostedBackdrop = frostedBackdrop,
        scrollSync = scrollSync,
        modifier = Modifier.testTag("wizard_relationship_axes_card")
    ) {
        val axisConfigs = listOf(
            Triple("affection", "Yakınlık (Sevgi)", Color(0xFFF472B6)),
            Triple("respect", "Saygı", Color(0xFFA78BFA)),
            Triple("comfort", "Rahatlık", Color(0xFF38BDF8)),
            Triple("resentment", "Kırgınlık", Color(0xFFFB7185))
        )
        axisConfigs.forEach { (key, label, accent) ->
            val value = reviewAxes[key] ?: 50
            WizardMetricSliderRow(
                label = label,
                value = value,
                accentColor = accent,
                onValueChange = { onAxisChange(key, it) }
            )
        }
    }

    Spacer(Modifier.height(12.dp))

    // 4. Attachment Style (Canonical: secure | anxious | avoidant | fearful_avoidant)
    WizardMorCamSectionCard(
        title = "Bağlanma Stili",
        subtitle = "Yakınlık, güven ve terk edilme karşısındaki refleks",
        frostedBackdrop = frostedBackdrop,
        scrollSync = scrollSync
    ) {
        val attachRow1 = listOf(
            "secure" to "Güvenli",
            "anxious" to "Kaygılı"
        )
        val attachRow2 = listOf(
            "avoidant" to "Kaçınan",
            "fearful_avoidant" to "Korkulu-Kaçınan"
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                attachRow1.forEach { (key, label) ->
                    WizardMorCamPillChip(
                        label = label,
                        selected = reviewAttachment == key,
                        onClick = { onAttachmentChange(key) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("wizard_attach_$key")
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                attachRow2.forEach { (key, label) ->
                    WizardMorCamPillChip(
                        label = label,
                        selected = reviewAttachment == key,
                        onClick = { onAttachmentChange(key) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("wizard_attach_$key")
                    )
                }
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    // 5. Dominant Emotion & Speech Style
    WizardMorCamSectionCard(
        title = "Baskın Duygu & Konuşma Tarzı",
        subtitle = "İlk sahnedeki ton ve hitap biçimi",
        frostedBackdrop = frostedBackdrop,
        scrollSync = scrollSync
    ) {
        WizardFieldLabel(title = "Baskın Duygu", required = false)
        Spacer(Modifier.height(6.dp))
        WizardMorCamTextField(
            value = reviewDominantEmotion,
            onValueChange = onDominantEmotionChange,
            placeholder = "Ör. Sakin ve mesafeli",
            singleLine = true,
            frostedBackdrop = frostedBackdrop,
            scrollSync = scrollSync,
            minHeight = 48.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("wizard_dominant_emotion_input")
        )
        Spacer(Modifier.height(12.dp))
        WizardFieldLabel(title = "Konuşma Tarzı", required = false)
        Spacer(Modifier.height(6.dp))
        WizardMorCamTextField(
            value = reviewSpeechStyle,
            onValueChange = onSpeechStyleChange,
            placeholder = "Ör. Kısa, net ve otoriter cümleler",
            singleLine = false,
            frostedBackdrop = frostedBackdrop,
            scrollSync = scrollSync,
            minHeight = 76.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("wizard_speech_style_input")
        )
    }

    Spacer(Modifier.height(12.dp))

    // 6. Sensitive Triggers (anchored to draftBotId)
    WizardMorCamSectionCard(
        title = "Hassas Konular",
        subtitle = "Karakteri duygusal olarak tetikleyen en kritik 3 unsur",
        frostedBackdrop = frostedBackdrop,
        scrollSync = scrollSync
    ) {
        reviewTriggers.forEachIndexed { idx, trig ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WizardMorCamTextField(
                    value = trig.triggerTopic,
                    onValueChange = { v ->
                        onTriggersChange(
                            reviewTriggers.toMutableList().also {
                                it[idx] = trig.copy(botId = draftBotId, triggerTopic = v)
                            }
                        )
                    },
                    placeholder = "Hassas konu...",
                    singleLine = true,
                    frostedBackdrop = frostedBackdrop,
                    scrollSync = scrollSync,
                    minHeight = 46.dp,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("wizard_trigger_input_$idx")
                )
                Spacer(Modifier.width(8.dp))
                MorCamCircleIconButton(
                    icon = Icons.Default.Close,
                    contentDescription = "Sil",
                    onClick = {
                        if (reviewTriggers.size > 1) {
                            onTriggersChange(reviewTriggers.toMutableList().also { it.removeAt(idx) })
                        }
                    },
                    size = 38.dp,
                    isDestructive = true
                )
            }
        }
        if (reviewTriggers.size < 3) {
            WizardMorCamPillChip(
                label = "+ Konu Ekle",
                selected = false,
                onClick = {
                    onTriggersChange(
                        reviewTriggers + SensitiveTriggerEntity(
                            botId = draftBotId,
                            triggerTopic = "",
                            sourceDescription = "Karakter geçmişi ve hassasiyetleri",
                            intensityMultiplier = 1.8
                        )
                    )
                }
            )
        }
    }

    Spacer(Modifier.height(12.dp))

    // 7. Collapsible Advanced Section: Big Five & Emotional Regulation Capacity
    WizardMorCamSectionCard(
        frostedBackdrop = frostedBackdrop,
        scrollSync = scrollSync,
        modifier = Modifier.testTag("wizard_advanced_psychology_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showAdvancedPsychology = !showAdvancedPsychology }
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Gelişmiş: Big Five & Duygu Direnci",
                    fontFamily = DisplaySerifFontFamily,
                    color = Color(0xFFFAF6FF),
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (showAdvancedPsychology) "Kişilik eksenlerini ve duygu kontrol kapasitesini gizle"
                    else "5 kişilik boyutu ve duygusal denge direncini özelleştir",
                    fontFamily = InterFontFamily,
                    color = Color(0xFFC6B6E8),
                    fontSize = 11.8.sp
                )
            }
            Icon(
                imageVector = if (showAdvancedPsychology) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (showAdvancedPsychology) "Daralt" else "Genişlet",
                tint = Color(0xFFD8B4FE),
                modifier = Modifier.size(22.dp)
            )
        }

        AnimatedVisibility(
            visible = showAdvancedPsychology,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(top = 14.dp)) {
                val bigFiveLabels = listOf(
                    "openness" to "Açıklık",
                    "conscientiousness" to "Sorumluluk",
                    "extraversion" to "Dışadönüklük",
                    "agreeableness" to "Uyumluluk",
                    "neuroticism" to "Nevrotiklik"
                )
                bigFiveLabels.forEach { (key, label) ->
                    val value = reviewBigFive[key] ?: 50
                    WizardMetricSliderRow(
                        label = label,
                        value = value,
                        accentColor = Color(0xFFC084FC),
                        onValueChange = { onBigFiveChange(key, it) }
                    )
                }

                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0x33D8B4FE))
                )
                Spacer(Modifier.height(10.dp))

                WizardMetricSliderRow(
                    label = "Duygu Direnci",
                    value = reviewRegulationCapacity,
                    valueRange = 10f..100f,
                    accentColor = Color(0xFF34D399),
                    onValueChange = onRegulationCapacityChange
                )
                Text(
                    text = "Yüksek duygu direnci, karakterin büyük öfke veya panik anlarında dışa vurumunu daha kontrollü tutmasını sağlar.",
                    fontFamily = InterFontFamily,
                    color = Color(0xFFBBA8E0),
                    fontSize = 11.2.sp,
                    lineHeight = 15.5.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    Spacer(Modifier.height(14.dp))

    // 8. Re-analyze & Reset Frosted Action Pills
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .clip(RoundedCornerShape(50))
                .templatesBackdropBlur(blurredBitmap = frostedBackdrop, alpha = 0.74f, scrollSync = scrollSync)
                .background(
                    Brush.horizontalGradient(listOf(Color(0x5E5B21B6), Color(0x4C3B1470)))
                )
                .border(1.15.dp, Color(0xCCE9D5FF), RoundedCornerShape(50))
                .clickable { onReanalyzeClick() }
                .testTag("wizard_reanalyze_button"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = Color(0xFFF3E8FF),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Yeniden Analiz",
                    fontFamily = InterFontFamily,
                    color = Color.White,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .clip(RoundedCornerShape(50))
                .templatesBackdropBlur(blurredBitmap = frostedBackdrop, alpha = 0.72f, scrollSync = scrollSync)
                .background(Color(0x441B0B36))
                .border(1.dp, Color(0x77C4B5FD), RoundedCornerShape(50))
                .clickable { onResetToDefaultClick() }
                .testTag("wizard_reset_analysis_button"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Varsayılana Dön",
                fontFamily = InterFontFamily,
                color = Color(0xFFE5D8FF),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun WizardMetricSliderRow(
    label: String,
    value: Int,
    accentColor: Color,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = InterFontFamily,
            color = Color(0xFFE5D8FF),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(104.dp)
        )
        MorCamGlassSlider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = valueRange,
            accentColor = accentColor,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .width(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x553B1A70))
                .border(0.9.dp, Color(0x99D8B4FE), RoundedCornerShape(10.dp))
                .padding(vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$value",
                fontFamily = InterFontFamily,
                color = Color.White,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
