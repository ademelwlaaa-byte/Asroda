@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.BotEntity
import com.example.data.repository.KeyCharacter
import com.example.ui.theme.EmochiPrimary
import com.example.ui.theme.EmochiTextMuted
import com.example.ui.theme.EmochiTextPrimary
import com.example.ui.theme.EmochiTextSecondary
import kotlinx.coroutines.launch
import java.util.Locale

internal object ModalInputValidators {
    private val TR_LOCALE: Locale = Locale.forLanguageTag("tr-TR")

    fun parseClampedTemperature(raw: String, min: Float = 0.1f, max: Float = 1.2f): Float? {
        val normalized = raw.trim().replace(',', '.')
        if (normalized.isEmpty()) return null
        val parsed = normalized.toFloatOrNull() ?: return null
        if (!parsed.isFinite()) return null
        return parsed.coerceIn(min, max)
    }

    fun sanitizePreviewText(raw: String, maxLength: Int = 260): String =
        StoryOverlayTextSanitizer.sanitizePreview(raw, maxLength = maxLength)

    fun extractAvatarInitial(displayName: String): String {
        val trimmed = displayName.trim()
        if (trimmed.isEmpty()) return "A"
        return trimmed.substring(0, 1).uppercase(TR_LOCALE)
    }
}

private val CollapsibleBgExpanded = Brush.linearGradient(listOf(Color(0xD91E0D3E), Color(0xD9110726)))
private val CollapsibleBgCollapsed = Brush.linearGradient(listOf(Color(0xBF15092E), Color(0xBF0D0620)))
private val CollapsibleBorderExpanded = Brush.linearGradient(
    listOf(Color(0xFFE9D5FF), Color(0xFF9333EA), Color(0xFF60A5FA), Color(0xFFC084FC))
)
private val CollapsibleBorderCollapsed = Brush.linearGradient(
    listOf(Color(0x66C084FC), Color(0x3360A5FA), Color(0x557C3AED))
)
private val CollapsibleIconBgExpanded = Brush.radialGradient(listOf(Color(0xFF3B1070), Color(0xFF1C0848)))
private val CollapsibleIconBgCollapsed = Brush.radialGradient(listOf(Color(0xFF1A0840), Color(0xFF0C0420)))
private val CollapsibleIconBorderExpanded = Brush.linearGradient(listOf(Color(0xFFC084FC), Color(0xFF7C3AED)))
private val CollapsibleIconBorderCollapsed = Brush.linearGradient(listOf(Color(0x40A855F7), Color(0x204C1D95)))
private val CollapsibleSeparatorBrush = Brush.horizontalGradient(
    listOf(Color.Transparent, Color(0x607C3AED), Color.Transparent)
)
private val ProfileAvatarFallbackBrush = Brush.radialGradient(listOf(Color(0x997C3AED), Color(0x662E1065)))
private val UpdateSuccessBadgeBrush = Brush.radialGradient(listOf(Color(0x998B5CF6), Color(0x553B1470)))
private val UpdateUpToDateBadgeBrush = Brush.radialGradient(listOf(Color(0x9934D399), Color(0x44065F46)))
private val FallbackModalTabs = listOf("Deneme Logları" to 0, "Güvenilirlik" to 1)

data class ModelSpec(
    val key: String,
    val name: String,
    val provider: String,
    val tokenCostRate: String,
    val badgeColor: Color,
    val description: String
)

@Composable
fun SettingsSectionHeader(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    color: Color = EmochiPrimary
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = title,
                color = EmochiTextPrimary,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = EmochiTextMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun CollapsibleSettingsOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    badgeText: String? = null,
    badgeColor: Color = EmochiPrimary,
    content: @Composable () -> Unit
) {
    val rowInteraction = remember { MutableInteractionSource() }
    val rowPressed by rowInteraction.collectIsPressedAsState()
    // Draw-phase State<Float> drivers -> 0 recompositions during press/chevron animations
    val rowScaleState = animateFloatAsState(
        targetValue = if (rowPressed) 0.985f else 1f,
        animationSpec = tween(80),
        label = "collScale"
    )
    val arrowRotationState = animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "arrowRot"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .graphicsLayer {
                val s = rowScaleState.value
                scaleX = s
                scaleY = s
            }
            .clip(RoundedCornerShape(16.dp))
            .background(if (isExpanded) CollapsibleBgExpanded else CollapsibleBgCollapsed)
            .border(
                BorderStroke(
                    if (isExpanded) 1.3.dp else 1.dp,
                    if (isExpanded) CollapsibleBorderExpanded else CollapsibleBorderCollapsed
                ),
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(interactionSource = rowInteraction, indication = null) { onToggle() }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isExpanded) CollapsibleIconBgExpanded else CollapsibleIconBgCollapsed)
                            .border(
                                if (isExpanded) 1.2.dp else 0.8.dp,
                                if (isExpanded) CollapsibleIconBorderExpanded else CollapsibleIconBorderCollapsed,
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = if (isExpanded) Color(0xFFCDA8FF) else EmochiTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(title, color = EmochiTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            if (badgeText != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(badgeColor.copy(alpha = 0.2f))
                                        .border(0.8.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(badgeText, color = badgeColor, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                        Text(
                            subtitle,
                            color = EmochiTextMuted,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer { rotationZ = arrowRotationState.value },
                    tint = if (isExpanded) Color(0xFFCDA8FF) else EmochiTextSecondary
                )
            }

            if (isExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(CollapsibleSeparatorBrush)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FallbackLogModal(
    logs: List<com.example.data.repository.ProviderFallbackLogEntry>,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Deneme Logları, 1 = Güvenilirlik
    val liveHealth by com.example.data.managers.ProviderHealthTracker.healthSnapshot.collectAsStateWithLifecycle()
    val sortedHealth = remember(liveHealth) { liveHealth.values.sortedBy { it.providerKey } }
    val modalContext = LocalContext.current

    MorCamDialogShell(
        onDismissRequest = onDismiss,
        title = "Sağlayıcı Deneme & Güvenilirlik",
        subtitle = "API geçiş geçmişi, devre kesici ve canlı metrikler",
        icon = Icons.AutoMirrored.Filled.List
    ) {
        // 2-tab Mor Cam switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0x45120826))
                .border(0.8.dp, Color(0x55B794F4), RoundedCornerShape(14.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FallbackModalTabs.forEach { (title, idx) ->
                val isSelected = selectedTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (isSelected) Color(0xAA7C3AED) else Color.Transparent)
                        .clickable { selectedTab = idx },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontFamily = com.example.ui.theme.InterFontFamily,
                        color = if (isSelected) Color(0xFFFDFBFF) else MorCamTokens.LavenderMuted,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val healthBannerText = remember(liveHealth, logs) {
            val summary = com.example.util.ProviderHealthSummaryFormatter.summarize(liveHealth)
            val fleetScore = com.example.util.ProviderHealthScoreCalculator.compute(liveHealth)
            val base = "Filo Puanı: ${fleetScore.score}/100 (${fleetScore.grade}) • ${com.example.util.ProviderHealthSummaryFormatter.formatBanner(summary)}"
            val redundancyLine = com.example.util.ProviderRoutingTierHelper.summarizeRedundancy(liveHealth.size)
            val rateLine = com.example.util.ProviderFallbackSuccessRateHelper.formatCompact(logs)
            val dominantErrorLine = com.example.util.ProviderErrorCategoryClassifier.summarizeDominantCategory(logs)
            val latestFailure = logs.firstOrNull { it.status == "FAILED" }
            val tipLine = if (latestFailure != null) {
                "💡 ${com.example.util.ProviderErrorRecoveryAdvisor.adviseForStatusCode(latestFailure.statusCode)}"
            } else ""
            listOf(base, redundancyLine, rateLine, dominantErrorLine, tipLine).filter { it.isNotBlank() }.joinToString("\n")
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x451D0E3A))
                .border(0.7.dp, Color(0x55C4B5FD), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = healthBannerText,
                fontFamily = com.example.ui.theme.InterFontFamily,
                color = MorCamTokens.SilverLavenderBright,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedTab == 0) {
            if (logs.isEmpty()) {
                MorCamGlassCard(
                    cornerRadius = 18.dp,
                    paddingVertical = 28.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MorCamTokens.LavenderSoft,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Henüz kayıtlı bir sağlayıcı deneme logu yok.",
                            fontFamily = com.example.ui.theme.InterFontFamily,
                            color = MorCamTokens.LavenderMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        items = logs,
                        key = { idx, entry -> "${entry.layer}_${entry.providerName}_${entry.status}_$idx" }
                    ) { _, entry ->
                        val isSuccess = entry.status == "SUCCESS"
                        val isFailed = entry.status == "FAILED"
                        val isRefusal = entry.status == "OOC_REFUSAL_FALLBACK"
                        val isCooldown = entry.status == "COOLDOWN_SKIP"
                        val badgeColor = when {
                            isSuccess -> Color(0xFFDDD6FE)
                            isFailed -> MorCamTokens.DestructiveRimLight
                            isRefusal || isCooldown -> Color(0xFFFDE68A)
                            else -> MorCamTokens.LavenderSoft
                        }
                        val badgeBg = when {
                            isSuccess -> Color(0x457C3AED)
                            isFailed -> Color(0x55831843)
                            isRefusal || isCooldown -> Color(0x5578350F)
                            else -> Color(0x38581C87)
                        }
                        val cleanError = remember(entry.errorMessage) {
                            entry.errorMessage?.let { ModalInputValidators.sanitizePreviewText(it, maxLength = 320) }.orEmpty()
                        }
                        MorCamGlassCard(
                            cornerRadius = 16.dp,
                            highlighted = isSuccess,
                            paddingHorizontal = 13.dp,
                            paddingVertical = 11.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "[Katman ${entry.layer}] ${entry.providerName}",
                                    fontFamily = com.example.ui.theme.LiterarySerifFontFamily,
                                    color = MorCamTokens.SilverLavenderBright,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(badgeBg)
                                        .border(0.8.dp, badgeColor.copy(alpha = 0.65f), RoundedCornerShape(50))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = entry.status,
                                        fontFamily = com.example.ui.theme.InterFontFamily,
                                        color = badgeColor,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            if (cleanError.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isRefusal) "Koruma: $cleanError" else "Detay: $cleanError",
                                    fontFamily = com.example.ui.theme.InterFontFamily,
                                    color = badgeColor.copy(alpha = 0.9f),
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (sortedHealth.isNotEmpty()) {
                    MorCamGlassCard(
                        cornerRadius = 16.dp,
                        highlighted = true,
                        paddingHorizontal = 12.dp,
                        paddingVertical = 10.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Devre Kesici & Canlı Sağlık",
                                fontFamily = com.example.ui.theme.LiterarySerifFontFamily,
                                color = MorCamTokens.SilverLavenderBright,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x667C3AED))
                                    .border(0.8.dp, Color(0xAAE9D5FF), RoundedCornerShape(10.dp))
                                    .clickable {
                                        com.example.data.managers.ProviderHealthTracker.resetAll()
                                        Toast.makeText(
                                            modalContext,
                                            "Tüm sağlayıcı devre kesicileri sıfırlandı",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Tümünü Sıfırla",
                                    fontFamily = com.example.ui.theme.InterFontFamily,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val now = System.currentTimeMillis()
                        sortedHealth.forEach { st ->
                            key(st.providerKey) {
                                val inCd = st.cooldownUntilMs > now
                                val rem = if (inCd) ((st.cooldownUntilMs - now) / 1000L).coerceAtLeast(1L) else 0L
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .then(
                                            if (inCd) Modifier.clickable {
                                                com.example.data.managers.ProviderHealthTracker.resetHealth(st.providerKey)
                                                Toast.makeText(
                                                    modalContext,
                                                    "${st.providerKey} devre kesici sıfırlandı",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else Modifier
                                        )
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = st.providerKey,
                                        fontFamily = com.example.ui.theme.InterFontFamily,
                                        color = MorCamTokens.SilverLavenderBright,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (inCd) {
                                            "⏳ ${st.lastFailureType?.name ?: "COOLDOWN"} (${rem}s) • Sıfırla"
                                        } else {
                                            "● Hazır • ${st.lastLatencyMs} ms • ${st.totalSuccessCount}✓/${st.totalFailureCount}✗"
                                        },
                                        fontFamily = com.example.ui.theme.InterFontFamily,
                                        color = if (inCd) Color(0xFFFDE68A) else Color(0xFF6EE7B7),
                                        fontSize = 10.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
                ProviderReliabilityDashboardUI()
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MorCamCapsuleButton(
                text = "📋 Raporu Kopyala",
                onClick = {
                    val report = com.example.util.ApiDiagnosticsFormatter.formatReport(
                        appVersion = com.example.BuildConfig.VERSION_NAME,
                        healthMap = liveHealth,
                        logs = logs
                    )
                    val cm = modalContext.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    cm?.setPrimaryClip(ClipData.newPlainText("Asroda API Raporu", report))
                    Toast.makeText(modalContext, "API tanılama raporu kopyalandı", Toast.LENGTH_SHORT).show()
                },
                variant = MorCamButtonVariant.Secondary,
                modifier = Modifier.weight(1.25f)
            )
            MorCamCapsuleButton(
                text = "Kapat",
                onClick = onDismiss,
                variant = MorCamButtonVariant.Secondary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun customTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MorCamTokens.LavenderLight,
    unfocusedBorderColor = MorCamTokens.LavenderSubtle.copy(alpha = 0.55f),
    focusedContainerColor = MorCamTokens.FieldSurfaceTop,
    unfocusedContainerColor = MorCamTokens.FieldSurfaceBottom,
    focusedTextColor = MorCamTokens.SilverLavenderBright,
    unfocusedTextColor = MorCamTokens.SilverLavenderBright,
    cursorColor = MorCamTokens.LavenderSoft
)

@Composable
fun FilterToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    MorCamSwitchRow(
        title = label,
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotQuickProfileSheet(
    bot: BotEntity,
    keyCharacters: List<KeyCharacter>,
    characterEmotions: List<com.example.data.local.CharacterEmotionEntity> = emptyList(),
    affectionEvents: List<com.example.data.local.AffectionEventEntity> = emptyList(),
    milestones: List<com.example.data.local.MilestoneEntity> = emptyList(),
    onDismiss: () -> Unit,
    onSaveBot: (BotEntity) -> Unit,
    onOpenFullSettings: () -> Unit,
    onResetChat: (Boolean) -> Unit,
    onExportChat: () -> Unit
) {
    var pinnedMemoryText by remember(bot.id, bot.pinnedMemory) { mutableStateOf(bot.pinnedMemory) }
    var storyNotesText by remember(bot.id, bot.storyNotes) { mutableStateOf(bot.storyNotes) }
    var showResetOptions by remember(bot.id) { mutableStateOf(false) }
    val isUniverse = bot.mode == "universe"
    val displayName = if (isUniverse) bot.universeName.ifBlank { "Evren" } else bot.aiName.ifBlank { "Karakter" }
    val avatarInitial = remember(displayName) { ModalInputValidators.extractAvatarInitial(displayName) }
    val bioPreview = remember(isUniverse, bot.scenario, bot.aiPersonality) {
        val raw = if (isUniverse) bot.scenario else bot.aiPersonality
        ModalInputValidators.sanitizePreviewText(raw, maxLength = 260).ifBlank { "Tanım eklenmemiş." }
    }
    val context = LocalContext.current

    MorCamDialogShell(
        onDismissRequest = onDismiss,
        title = displayName,
        subtitle = if (isUniverse) "✦ Evren Senaryosu" else "✦ Bireysel Karakter",
        icon = if (isUniverse) Icons.Default.Public else Icons.Default.Person
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 540.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Character Avatar & Personality Frosted Summary Card
            MorCamGlassCard(highlighted = true, cornerRadius = 18.dp) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(ProfileAvatarFallbackBrush)
                            .border(1.4.dp, MorCamTokens.RimGradient, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        BotAvatarImage(
                            avatarUrl = bot.avatarUrl.takeIf { it.isNotBlank() },
                            contentDescription = displayName,
                            modifier = Modifier.fillMaxSize(),
                            fallback = {
                                Text(
                                    text = avatarInitial,
                                    fontFamily = com.example.ui.theme.DisplaySerifFontFamily,
                                    color = MorCamTokens.SilverLavenderBright,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isUniverse) "Evren Senaryosu" else "Karakter Kişiliği",
                            fontFamily = com.example.ui.theme.LiterarySerifFontFamily,
                            color = MorCamTokens.LavenderSoft,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = bioPreview,
                            fontFamily = com.example.ui.theme.InterFontFamily,
                            color = MorCamTokens.SilverLavenderBright.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            val quickVm: com.example.ui.viewmodel.EmochiViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            val quickTriggers by quickVm.sensitiveTriggers.collectAsStateWithLifecycle()
            val quickReappraisals by quickVm.pendingReappraisals.collectAsStateWithLifecycle()
            var quickCastMembers by remember(bot.id) { mutableStateOf<List<com.example.data.local.CastMemberEntity>>(emptyList()) }
            LaunchedEffect(bot.id) {
                quickVm.loadEmotionalDefenseState(bot.id)
                quickCastMembers = try {
                    quickVm.getCastMembersForBot(bot.id).filter { !it.isBlacklisted }
                } catch (_: Exception) {
                    emptyList()
                }
            }

            EmotionStatusSection(
                bot = bot,
                characterEmotions = characterEmotions,
                keyCharacters = keyCharacters,
                castMembers = quickCastMembers,
                affectionEvents = affectionEvents,
                onUpdateCharacterEmotion = { charName, mood, aff, trust, tension ->
                    quickVm.updateCharacterEmotion(bot.id, charName, mood, aff, trust, tension)
                },
                sensitiveTriggers = quickTriggers,
                pendingReappraisals = quickReappraisals,
                onAddTrigger = { kw -> quickVm.addSensitiveTrigger(bot.id, kw) },
                onDeleteTrigger = { id -> quickVm.deleteSensitiveTrigger(bot.id, id) }
            )

            com.example.ui.screens.StoryMilestoneTimelineCard(
                milestones = milestones
            )

            // Pinned Memory Quick Editor
            MorCamGlassCard(cornerRadius = 18.dp) {
                Text(
                    text = "✦ Kalıcı Hafıza (Sabit Notlar)",
                    fontFamily = com.example.ui.theme.LiterarySerifFontFamily,
                    color = MorCamTokens.SilverLavenderBright,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "AI'nın unutmasını istemediğiniz detayları buraya yazın.",
                    fontFamily = com.example.ui.theme.InterFontFamily,
                    color = MorCamTokens.LavenderMuted,
                    fontSize = 11.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                MorCamGlassTextField(
                    value = pinnedMemoryText,
                    onValueChange = { pinnedMemoryText = it },
                    placeholder = "Kalıcı notlar ekle...",
                    singleLine = false,
                    minHeight = 84.dp
                )
                Spacer(modifier = Modifier.height(10.dp))
                MorCamCapsuleButton(
                    text = "Hafızayı Kaydet",
                    icon = Icons.Default.Save,
                    onClick = {
                        val cleanedPinned = pinnedMemoryText.trim()
                        pinnedMemoryText = cleanedPinned
                        onSaveBot(bot.copy(pinnedMemory = cleanedPinned, storyNotes = storyNotesText.trim()))
                        Toast.makeText(context, "Hafıza güncellendi ✨", Toast.LENGTH_SHORT).show()
                    },
                    variant = MorCamButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            MorCamDivider()

            Text(
                text = "Hızlı İşlemler",
                fontFamily = com.example.ui.theme.LiterarySerifFontFamily,
                color = MorCamTokens.LavenderLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MorCamCapsuleButton(
                    text = "Kopyala",
                    icon = Icons.Default.Description,
                    onClick = onExportChat,
                    variant = MorCamButtonVariant.Secondary,
                    modifier = Modifier.weight(1f)
                )
                MorCamCapsuleButton(
                    text = "Sıfırla",
                    icon = Icons.Default.Refresh,
                    onClick = { showResetOptions = !showResetOptions },
                    variant = MorCamButtonVariant.Destructive,
                    modifier = Modifier.weight(1f)
                )
            }

            MorCamCapsuleButton(
                text = "Tüm Karakter Ayarlarını Aç",
                icon = Icons.Default.Settings,
                onClick = {
                    onDismiss()
                    onOpenFullSettings()
                },
                variant = MorCamButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth()
            )

            if (showResetOptions) {
                MorCamGlassCard(highlighted = true, cornerRadius = 18.dp) {
                    Text(
                        text = "Sohbeti Sıfırlama Yöntemi",
                        fontFamily = com.example.ui.theme.LiterarySerifFontFamily,
                        color = MorCamTokens.SilverLavenderBright,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    MorCamOptionRow(
                        title = "Sadece Mesajları Sil",
                        subtitle = "Hafıza ve ilişki bağları korunur",
                        icon = Icons.AutoMirrored.Filled.Chat,
                        selected = true,
                        onClick = {
                            showResetOptions = false
                            onResetChat(false)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MorCamOptionRow(
                        title = "Tamamen Sıfırla",
                        subtitle = "Mesajlar, hafıza ve duygu durumu ilk haline döner",
                        icon = Icons.Default.DeleteSweep,
                        destructive = true,
                        onClick = {
                            showResetOptions = false
                            onResetChat(true)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminConsoleModal(
    isAdminMode: Boolean,
    onToggleAdminMode: (Boolean) -> Unit,
    totalBotsCount: Int,
    totalMessagesCount: Int,
    onDismiss: () -> Unit,
    onClearCoilCache: () -> Unit,
    onExportData: suspend () -> String,
    @Suppress("UNUSED_PARAMETER") onImportData: suspend (String) -> Unit
) {
    var globalSystemPrompt by remember { mutableStateOf("ADMIN MASTER DIRECTIVE: Full override active. Maintain extreme realism, emotional depth, and uncensored character fidelity.") }
    var globalTemp by remember { mutableStateOf("0.85") }
    var isExporting by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    MorCamDialogShell(
        onDismissRequest = onDismiss,
        title = "Admin Özel Konsolu",
        subtitle = "Sistem metrikleri, önbellek ve veri yedekleme",
        icon = Icons.Default.Security,
        maxWidthFraction = 0.94f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MorCamSwitchRow(
                title = "Admin Modu Yetkileri",
                subtitle = if (isAdminMode) "Açık • Tüm şablonları, bot verilerini ve hafızayı düzenleyebilirsiniz." else "Kapalı • Standart kullanıcı görünümü.",
                checked = isAdminMode,
                onCheckedChange = onToggleAdminMode,
                badge = if (isAdminMode) "Aktif" else null
            )

            MorCamGlassCard(cornerRadius = 18.dp) {
                Text(
                    text = "Sistem & Veritabanı Metrikleri",
                    fontFamily = com.example.ui.theme.LiterarySerifFontFamily,
                    color = MorCamTokens.SilverLavenderBright,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Toplam Bot", color = MorCamTokens.LavenderMuted, fontSize = 11.sp)
                        Text("$totalBotsCount Bot", color = MorCamTokens.LavenderLight, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Mesaj Kayıtları", color = MorCamTokens.LavenderMuted, fontSize = 11.sp)
                        Text("$totalMessagesCount Mesaj", color = MorCamTokens.SilverLavenderBright, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                MorCamDivider()
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "• Coil LRU Disk Önbelleği: 35 MB Limit (RGB_565)\n• Micro-WebP Avatar Optimizasyonu: 160x160px\n• Yerel Depolama: internal/bot_images/",
                    color = MorCamTokens.LavenderMuted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                MorCamCapsuleButton(
                    text = "Görsel Önbelleğini Temizle",
                    onClick = {
                        onClearCoilCache()
                        Toast.makeText(context, "Coil görsel önbelleği temizlendi!", Toast.LENGTH_SHORT).show()
                    },
                    variant = MorCamButtonVariant.Secondary,
                    icon = Icons.Default.DeleteSweep,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            MorCamGlassCard(cornerRadius = 18.dp) {
                Text(
                    text = "Global AI Yönergesi & Sıcaklık",
                    fontFamily = com.example.ui.theme.LiterarySerifFontFamily,
                    color = MorCamTokens.SilverLavenderBright,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                MorCamGlassTextField(
                    value = globalSystemPrompt,
                    onValueChange = { globalSystemPrompt = it },
                    placeholder = "Global sistem yönergesi...",
                    singleLine = false,
                    minHeight = 76.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MorCamGlassTextField(
                        value = globalTemp,
                        onValueChange = { globalTemp = it },
                        placeholder = "Sıcaklık (0.1 - 1.2)",
                        modifier = Modifier.weight(1f)
                    )
                    MorCamCapsuleButton(
                        text = "Uygula",
                        onClick = {
                            val clampedTemp = ModalInputValidators.parseClampedTemperature(globalTemp)
                            if (clampedTemp == null) {
                                Toast.makeText(context, "Geçerli bir sıcaklık girin (0.1 - 1.2)", Toast.LENGTH_SHORT).show()
                            } else {
                                globalTemp = String.format(Locale.US, "%.2f", clampedTemp)
                                Toast.makeText(context, "Admin Master AI parametreleri güncellendi ($globalTemp)!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        variant = MorCamButtonVariant.Primary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MorCamCapsuleButton(
                    text = if (isExporting) "Aktarılıyor..." else "DB Dışa Aktar",
                    onClick = {
                        if (isExporting) return@MorCamCapsuleButton
                        coroutineScope.launch {
                            isExporting = true
                            try {
                                val json = onExportData()
                                if (json.isBlank()) {
                                    Toast.makeText(context, "Dışa aktarılacak veri bulunamadı", Toast.LENGTH_SHORT).show()
                                } else {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    clipboard?.setPrimaryClip(ClipData.newPlainText("Emochi_Full_DB", json))
                                    Toast.makeText(context, "Tüm DB JSON pano kopyalandı!", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Dışa aktarma hatası: ${e.message ?: "Bilinmeyen hata"}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isExporting = false
                            }
                        }
                    },
                    variant = MorCamButtonVariant.Primary,
                    modifier = Modifier.weight(1f)
                )
                MorCamCapsuleButton(
                    text = "Kapat",
                    onClick = onDismiss,
                    variant = MorCamButtonVariant.Secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun UpdateCheckerModal(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentVersion = com.example.BuildConfig.VERSION_NAME
    var checkTick by remember { mutableIntStateOf(0) }
    var isChecking by remember { mutableStateOf(true) }
    var checkResult by remember { mutableStateOf<com.example.util.AppUpdateManager.CheckResult?>(null) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadedBytes by remember { mutableLongStateOf(0L) }
    var totalBytes by remember { mutableLongStateOf(0L) }
    var downloadedApk by remember { mutableStateOf<java.io.File?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(checkTick) {
        isChecking = true
        statusMessage = null
        try {
            checkResult = com.example.util.AppUpdateManager.checkForUpdate(currentVersion)
        } catch (e: Exception) {
            checkResult = com.example.util.AppUpdateManager.CheckResult.Error(
                e.message ?: "Sürüm kontrolü sırasında beklenmeyen bir hata oluştu."
            )
        } finally {
            isChecking = false
        }
    }

    MorCamDialogShell(
        onDismissRequest = onDismiss,
        title = "Asroda Güncelleme Merkezi",
        subtitle = "Mevcut sürüm: v$currentVersion • Resmi Release Kanalı",
        icon = Icons.Default.AutoAwesome
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isChecking) {
                Spacer(modifier = Modifier.height(10.dp))
                EmochiLoadingSpinner(size = 38.dp, color = MorCamTokens.LavenderSoft)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Resmi Asroda sürüm sunucusu kontrol ediliyor...",
                    fontFamily = com.example.ui.theme.InterFontFamily,
                    color = MorCamTokens.LavenderMuted,
                    fontSize = 12.5.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
            } else if (isDownloading) {
                val fraction = if (totalBytes > 0L) (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f
                Text(
                    text = "Yeni Asroda APK İndiriliyor...",
                    fontFamily = com.example.ui.theme.LiterarySerifFontFamily,
                    color = MorCamTokens.SilverLavenderBright,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0x551F0B3F))
                        .border(0.8.dp, MorCamTokens.SubtleRimGradient, RoundedCornerShape(50))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(MorCamTokens.PrimaryButtonFill)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                val progressText = if (totalBytes > 0L) {
                    "%${(fraction * 100).toInt()} • ${com.example.util.AppUpdateManager.formatSizeMb(downloadedBytes)} / ${com.example.util.AppUpdateManager.formatSizeMb(totalBytes)}"
                } else {
                    "${com.example.util.AppUpdateManager.formatSizeMb(downloadedBytes)} indirildi..."
                }
                Text(
                    text = progressText,
                    fontFamily = com.example.ui.theme.InterFontFamily,
                    color = MorCamTokens.LavenderMuted,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
            } else if (downloadedApk != null) {
                val apkFile = downloadedApk!!
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(UpdateSuccessBadgeBrush)
                        .border(1.2.dp, MorCamTokens.RimGradient, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MorCamTokens.SilverLavenderBright,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "APK İndirildi — Kuruluma Hazır",
                    fontFamily = com.example.ui.theme.DisplaySerifFontFamily,
                    color = MorCamTokens.SilverLavenderBright,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = statusMessage ?: "Android kurulum ekranı açıldı. Tüm botlarınız, sohbetleriniz ve hikâye kayıtlarınız korunur.",
                    fontFamily = com.example.ui.theme.InterFontFamily,
                    color = MorCamTokens.LavenderMuted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                MorCamCapsuleButton(
                    text = "Kurulumu Başlat",
                    icon = Icons.Default.FlashOn,
                    onClick = {
                        val launched = com.example.util.AppUpdateManager.launchApkInstaller(context, apkFile)
                        statusMessage = if (launched) {
                            "Android kurulum ekranı açıldı."
                        } else {
                            "Önce 'Bilinmeyen uygulamalara izin ver' ayarını açıp geri dönün, ardından tekrar basın."
                        }
                    },
                    variant = MorCamButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                when (val res = checkResult) {
                    is com.example.util.AppUpdateManager.CheckResult.UpdateAvailable -> {
                        val rel = res.release
                        val notesText = remember(rel.releaseNotes) {
                            rel.releaseNotes.trim().ifBlank { "Yeni iyileştirmeler ve hata düzeltmeleri." }
                        }
                        val notesScrollState = rememberScrollState()
                        MorCamGlassCard(
                            highlighted = true,
                            cornerRadius = 18.dp,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "✦ Yeni Sürüm Hazır",
                                    fontFamily = com.example.ui.theme.LiterarySerifFontFamily,
                                    color = MorCamTokens.SilverLavenderBright,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(Color(0x457C3AED))
                                        .border(0.8.dp, MorCamTokens.LavenderLight.copy(alpha = 0.7f), RoundedCornerShape(50))
                                        .padding(horizontal = 9.dp, vertical = 3.dp)
                                ) {
                                    val sizeLabel = com.example.util.AppUpdateManager.formatSizeMb(rel.apkSizeBytes)
                                    Text(
                                        text = if (sizeLabel.isNotBlank()) "v${rel.versionName} • $sizeLabel" else "v${rel.versionName}",
                                        fontFamily = com.example.ui.theme.InterFontFamily,
                                        color = MorCamTokens.SilverLavenderBright,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false)
                                    .heightIn(max = 260.dp)
                                    .verticalScroll(notesScrollState)
                            ) {
                                Text(
                                    text = notesText,
                                    fontFamily = com.example.ui.theme.InterFontFamily,
                                    color = MorCamTokens.LavenderMuted,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        statusMessage?.let { err ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "⚠ $err",
                                fontFamily = com.example.ui.theme.InterFontFamily,
                                color = Color(0xFFFCA5A5),
                                fontSize = 11.5.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        MorCamCapsuleButton(
                            text = "İndir ve Kur (v${rel.versionName})",
                            icon = Icons.Default.FlashOn,
                            onClick = {
                                if (isDownloading) return@MorCamCapsuleButton
                                coroutineScope.launch {
                                    isDownloading = true
                                    downloadedBytes = 0L
                                    totalBytes = rel.apkSizeBytes
                                    statusMessage = null
                                    try {
                                        val dlRes = com.example.util.AppUpdateManager.downloadApk(
                                            context = context,
                                            release = rel,
                                            onProgress = { dl, tot ->
                                                downloadedBytes = dl
                                                totalBytes = tot
                                            }
                                        )
                                        dlRes.fold(
                                            onSuccess = { file ->
                                                downloadedApk = file
                                                val launched = com.example.util.AppUpdateManager.launchApkInstaller(context, file)
                                                statusMessage = if (launched) {
                                                    "Android kurulum ekranı açıldı. Tüm sohbet ve hikâye verileriniz korunur."
                                                } else {
                                                    "Lütfen 'Bilinmeyen kaynaklardan kurulum' iznini verip geri dönün, ardından Kurulumu Başlat'a dokunun."
                                                }
                                            },
                                            onFailure = { e ->
                                                statusMessage = e.message ?: "APK indirilemedi."
                                            }
                                        )
                                    } finally {
                                        isDownloading = false
                                    }
                                }
                            },
                            variant = MorCamButtonVariant.Primary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    is com.example.util.AppUpdateManager.CheckResult.UpToDate -> {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(UpdateUpToDateBadgeBrush)
                                .border(1.2.dp, Color(0xFF6EE7B7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFFD1FAE5),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Asroda Güncel (v${res.currentVersion})",
                            fontFamily = com.example.ui.theme.DisplaySerifFontFamily,
                            color = MorCamTokens.SilverLavenderBright,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Şu anda yayınlanan en güncel Asroda sürümünü kullanıyorsunuz.",
                            fontFamily = com.example.ui.theme.InterFontFamily,
                            color = MorCamTokens.LavenderMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        MorCamCapsuleButton(
                            text = "Tekrar Kontrol Et",
                            icon = Icons.Default.Refresh,
                            onClick = { checkTick++ },
                            variant = MorCamButtonVariant.Secondary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    is com.example.util.AppUpdateManager.CheckResult.Error -> {
                        Text(
                            text = "⚠ ${res.message}",
                            fontFamily = com.example.ui.theme.InterFontFamily,
                            color = Color(0xFFFCA5A5),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        MorCamCapsuleButton(
                            text = "Tekrar Dene",
                            icon = Icons.Default.Refresh,
                            onClick = { checkTick++ },
                            variant = MorCamButtonVariant.Primary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    null -> Unit
                }
            }
        }
    }
}

@Composable
fun ProviderReliabilityDashboardUI() {
    val statsList = remember { com.example.util.ProviderRateLimitTracker.getAllSecondaryStats() }

    MorCamGlassCard(
        cornerRadius = 18.dp,
        highlighted = true,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = MorCamTokens.LavenderSoft,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sağlayıcı Güvenilirlik Panosu",
                    fontFamily = com.example.ui.theme.LiterarySerifFontFamily,
                    color = MorCamTokens.SilverLavenderBright,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x457C3AED))
                    .border(0.7.dp, MorCamTokens.LavenderSoft.copy(alpha = 0.6f), RoundedCornerShape(50))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "CANLI",
                    fontFamily = com.example.ui.theme.InterFontFamily,
                    color = MorCamTokens.LavenderLight,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Ücretsiz ve yedek katmandaki sağlayıcıların anlık performans ve kota metrikleri:",
            fontFamily = com.example.ui.theme.InterFontFamily,
            color = MorCamTokens.LavenderMuted,
            fontSize = 10.5.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        statsList.forEach { stat ->
            key(stat.displayName) {
                MorCamGlassCard(
                    cornerRadius = 14.dp,
                    paddingHorizontal = 11.dp,
                    paddingVertical = 9.dp,
                    modifier = Modifier.padding(vertical = 3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stat.displayName,
                            fontFamily = com.example.ui.theme.LiterarySerifFontFamily,
                            color = MorCamTokens.SilverLavenderBright,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        val statusBg = if (stat.isLimitApproaching) Color(0x55831843) else Color(0x456D28D9)
                        val statusTxt = if (stat.isLimitApproaching) MorCamTokens.DestructiveRimLight else MorCamTokens.LavenderLight
                        val label = if (stat.isLimitApproaching) "Kısıt Yakın" else "Aktif"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(statusBg)
                                .border(0.7.dp, statusTxt.copy(alpha = 0.6f), RoundedCornerShape(50))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = label,
                                fontFamily = com.example.ui.theme.InterFontFamily,
                                color = statusTxt,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Dakika / Saat İstek:", color = MorCamTokens.LavenderMuted, fontSize = 10.sp)
                        Text(
                            "${stat.requestsLastMinute} req/dk • ${stat.requestsLastHour} req/saat",
                            color = MorCamTokens.SilverLavenderBright,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Token / Kredi:", color = MorCamTokens.LavenderMuted, fontSize = 10.sp)
                        val tokenLabel = if (stat.quotaType == com.example.util.QuotaType.EXHAUSTIBLE) {
                            "Kalan Kredi: ${stat.creditsRemaining}"
                        } else {
                            val headroom = com.example.util.ProviderRateLimitBudgetHelper.evaluate(stat.tokensUsed24h, stat.tokenLimit24h)
                            "${stat.tokensUsed24h} / ${stat.tokenLimit24h} (${headroom.statusLabel} %${headroom.remainingPercent})"
                        }
                        Text(tokenLabel, color = MorCamTokens.LavenderSoft, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Ort. Yanıt • Hata • Devir:", color = MorCamTokens.LavenderMuted, fontSize = 10.sp)
                        val speedBadge = com.example.util.ProviderLatencyBadgeFormatter.formatBadge(stat.averageResponseTimeMs)
                        Text(
                            "$speedBadge • ${stat.malformedCount} hata • ${stat.fallbackCount} devir",
                            color = MorCamTokens.SilverLavenderBright,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
