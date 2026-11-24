package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.LorebookEntity
import com.example.data.local.LorebookEntryEntity
import com.example.data.managers.LorebookManager
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LiterarySerifFontFamily
import com.example.ui.viewmodel.EmochiViewModel
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Faz 3 — Dinamik Lorebook & Paylaşılabilir Evren Ansiklopedisi Yönetim Modalı.
 * 100% cihaz-içi Room tabanlı; yalnızca RP Sohbet promptuna akıllı token bütçesiyle enjekte edilir.
 */
@Composable
fun LorebookManagerModal(
    botId: String,
    botName: String,
    viewModel: EmochiViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val allLorebooks by viewModel.allLorebooks.collectAsStateWithLifecycle(initialValue = emptyList())
    val linkedIdsFlow = remember(botId) { viewModel.getLinkedLorebookIdsFlow(botId) }
    val linkedIds by linkedIdsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val linkedSet = remember(linkedIds) { linkedIds.toSet() }

    var selectedLorebookId by remember { mutableStateOf<String?>(null) }
    val selectedLorebook = remember(allLorebooks, selectedLorebookId) {
        allLorebooks.firstOrNull { it.id == selectedLorebookId }
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var newLbName by remember { mutableStateOf("") }
    var newLbDesc by remember { mutableStateOf("") }

    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }

    var editingEntry by remember { mutableStateOf<LorebookEntryEntity?>(null) }
    var testQuery by remember { mutableStateOf("") }
    var showSimulator by remember { mutableStateOf(false) }

    MorCamDialogShell(
        onDismissRequest = onDismiss,
        title = if (selectedLorebook == null) "Evren Ansiklopedisi (Lorebook)" else selectedLorebook.name,
        subtitle = if (selectedLorebook == null) {
            "$botName için bağlı ansiklopediler (${linkedSet.size} aktif)"
        } else {
            "Anahtar kelime & sabit tetiklemeli dinamik RP bilgisi"
        },
        icon = Icons.AutoMirrored.Filled.MenuBook,
        maxWidthFraction = 0.95f
    ) {
        if (selectedLorebook == null) {
            // ── VIEW 1: LOREBOOK PACK LIST ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 470.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MorCamChipPill(
                        label = "+ Yeni Paket",
                        selected = true,
                        icon = Icons.Default.Add,
                        onClick = {
                            newLbName = ""
                            newLbDesc = ""
                            showCreateDialog = true
                        },
                        modifier = Modifier.weight(1f).testTag("lorebook_create_btn")
                    )
                    MorCamChipPill(
                        label = "JSON İçe Aktar",
                        selected = false,
                        icon = Icons.Default.FileUpload,
                        onClick = {
                            importJsonText = ""
                            showImportDialog = true
                        },
                        modifier = Modifier.weight(1f).testTag("lorebook_import_btn")
                    )
                }

                // 1-Tap Nevermore Starter Pack Banner
                val hasNevermorePack = allLorebooks.any {
                    it.name.equals("Nevermore & Blackwood Mirası", ignoreCase = true)
                }
                if (!hasNevermorePack) {
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "✦ Nevermore & Blackwood Hazır Paketi",
                                    fontFamily = DisplaySerifFontFamily,
                                    color = MorCamTokens.SilverLavenderBright,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "5 Güç Formu, Galatasaray kolyesi, bedeller ve 6 hazır madde tek dokunuşla.",
                                    fontFamily = InterFontFamily,
                                    color = MorCamTokens.LavenderMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            MorCamChipPill(
                                label = "Yükle",
                                selected = true,
                                onClick = {
                                    scope.launch {
                                        val installed = viewModel.installNevermoreStarterLorebook(linkToBotId = botId)
                                        selectedLorebookId = installed.id
                                        Toast.makeText(context, "Nevermore ansiklopedisi eklendi ve bağlandı ✦", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.testTag("lorebook_install_nevermore_btn")
                            )
                        }
                    }
                }

                if (allLorebooks.isEmpty()) {
                    MorCamGlassCard(cornerRadius = 16.dp, paddingHorizontal = 14.dp, paddingVertical = 16.dp) {
                        Text(
                            text = "Henüz bir Evren Ansiklopedisi oluşturulmadı.",
                            fontFamily = DisplaySerifFontFamily,
                            color = MorCamTokens.SilverLavenderBright,
                            fontSize = 13.5.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Aynı Lorebook paketini birden fazla RP sohbetine bağlayabilirsin. Anahtar kelimeler sohbette geçtikçe ilgili bilgiler akıllı token bütçesiyle otomatik hatırlanır.",
                            fontFamily = InterFontFamily,
                            color = MorCamTokens.LavenderMuted,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )
                    }
                } else {
                    allLorebooks.forEach { lb ->
                        val isLinked = lb.id in linkedSet
                        MorCamGlassCard(
                            cornerRadius = 16.dp,
                            highlighted = isLinked,
                            paddingHorizontal = 12.dp,
                            paddingVertical = 10.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedLorebookId = lb.id },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = lb.name,
                                        fontFamily = DisplaySerifFontFamily,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (lb.description.isNotBlank()) {
                                        Text(
                                            text = lb.description,
                                            fontFamily = InterFontFamily,
                                            color = MorCamTokens.LavenderMuted,
                                            fontSize = 11.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "Token Bütçesi: ${lb.tokenBudget} • ${if (lb.isEnabled) "Aktif" else "Pasif"}",
                                        fontFamily = InterFontFamily,
                                        color = MorCamTokens.LavenderLight,
                                        fontSize = 10.5.sp
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    MorCamChipPill(
                                        label = if (isLinked) "Bağlı ✓" else "Bağla",
                                        selected = isLinked,
                                        onClick = {
                                            viewModel.setLorebookLinkedToBot(botId, lb.id, !isLinked)
                                        },
                                        modifier = Modifier.testTag("lorebook_link_toggle_${lb.id}")
                                    )
                                    Text(
                                        text = "Maddeleri Aç ›",
                                        fontFamily = InterFontFamily,
                                        color = Color(0xFFD8B4FE),
                                        fontSize = 11.sp,
                                        modifier = Modifier.clickable { selectedLorebookId = lb.id }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ── VIEW 2: SELECTED LOREBOOK ENTRIES & SIMULATOR ──
            val entriesFlow = remember(selectedLorebook.id) {
                viewModel.getLorebookEntriesFlow(selectedLorebook.id)
            }
            val entries by entriesFlow.collectAsStateWithLifecycle(initialValue = emptyList())
            var budgetSlider by remember(selectedLorebook.id, selectedLorebook.tokenBudget) {
                mutableFloatStateOf(selectedLorebook.tokenBudget.toFloat())
            }
            val isLinked = selectedLorebook.id in linkedSet

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Navigation & Pack Actions Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MorCamChipPill(
                        label = "Listeye Dön",
                        selected = false,
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        onClick = {
                            selectedLorebookId = null
                            showSimulator = false
                        }
                    )
                    MorCamChipPill(
                        label = if (isLinked) "Bu Bota Bağlı ✓" else "Bu Bota Bağla",
                        selected = isLinked,
                        onClick = {
                            viewModel.setLorebookLinkedToBot(botId, selectedLorebook.id, !isLinked)
                        }
                    )
                    MorCamChipPill(
                        label = "JSON Kopyala",
                        selected = false,
                        icon = Icons.Default.ContentCopy,
                        onClick = {
                            scope.launch {
                                val exported = viewModel.exportLorebookToJson(selectedLorebook.id)
                                if (!exported.isNullOrBlank()) {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cm.setPrimaryClip(ClipData.newPlainText("Lorebook JSON", exported))
                                    Toast.makeText(context, "Lorebook JSON panoya kopyalandı ✦", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                    MorCamChipPill(
                        label = "Sil",
                        selected = false,
                        icon = Icons.Default.Delete,
                        onClick = {
                            val delId = selectedLorebook.id
                            selectedLorebookId = null
                            viewModel.deleteLorebook(delId)
                            Toast.makeText(context, "Ansiklopedi silindi", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Token Budget & Active Toggle Card
                MorCamGlassCard(cornerRadius = 14.dp, paddingHorizontal = 12.dp, paddingVertical = 10.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Akıllı Token Bütçesi: ${budgetSlider.toInt()} tok",
                            fontFamily = LiterarySerifFontFamily,
                            color = MorCamTokens.SilverLavenderBright,
                            fontSize = 12.sp
                        )
                        Switch(
                            checked = selectedLorebook.isEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.upsertLorebook(selectedLorebook.copy(isEnabled = enabled))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF9333EA)
                            )
                        )
                    }
                    Slider(
                        value = budgetSlider,
                        onValueChange = { budgetSlider = it },
                        onValueChangeFinished = {
                            viewModel.upsertLorebook(selectedLorebook.copy(tokenBudget = budgetSlider.toInt()))
                        },
                        valueRange = 200f..1500f,
                        steps = 25,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFD8B4FE),
                            activeTrackColor = Color(0xFFA855F7)
                        )
                    )
                }

                // Add Entry + Live Simulator Toggle Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MorCamChipPill(
                        label = "+ Madde Ekle (${entries.size})",
                        selected = true,
                        icon = Icons.Default.Add,
                        onClick = {
                            editingEntry = LorebookEntryEntity(
                                id = UUID.randomUUID().toString(),
                                lorebookId = selectedLorebook.id,
                                title = "",
                                keywords = "",
                                content = "",
                                category = "genel",
                                priority = 6,
                                isConstant = false,
                                isEnabled = true,
                                scanDepth = 4
                            )
                        },
                        modifier = Modifier.weight(1f).testTag("lorebook_add_entry_btn")
                    )
                    MorCamChipPill(
                        label = if (showSimulator) "Testi Gizle" else "Tetikleme Testi",
                        selected = showSimulator,
                        icon = Icons.Default.Search,
                        onClick = { showSimulator = !showSimulator },
                        modifier = Modifier.weight(1f).testTag("lorebook_simulator_toggle_btn")
                    )
                }

                // Live Trigger Simulator
                AnimatedVisibility(visible = showSimulator) {
                    val simResult = remember(entries, testQuery, selectedLorebook.tokenBudget) {
                        LorebookManager.selectEntriesForContext(
                            entries = entries,
                            recentMessageTexts = if (testQuery.isBlank()) emptyList() else listOf(testQuery),
                            tokenBudget = selectedLorebook.tokenBudget,
                            allowChainedTriggering = true
                        )
                    }
                    MorCamGlassCard(cornerRadius = 14.dp, highlighted = true, paddingHorizontal = 12.dp, paddingVertical = 10.dp) {
                        LorebookLabeledField(
                            value = testQuery,
                            onValueChange = { testQuery = it },
                            label = "Örnek Sahne / Mesaj Metni",
                            placeholder = "Örn: Wednesday kolye hakkında soru sordu...",
                            singleLine = true
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Tetiklenen: ${simResult.selected.size} madde (~${simResult.estimatedTokens} token)",
                            fontFamily = InterFontFamily,
                            color = Color(0xFFE9D5FF),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        simResult.selected.forEach { hit ->
                            Text(
                                text = "• ${hit.entry.title} (${hit.reason}, öncelik ${hit.entry.priority})",
                                fontFamily = InterFontFamily,
                                color = MorCamTokens.LavenderLight,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Entries List
                if (entries.isEmpty()) {
                    Text(
                        text = "Bu ansiklopedide henüz madde yok. '+ Madde Ekle' ile karakter, mekan veya evren kuralları ekle.",
                        fontFamily = InterFontFamily,
                        color = MorCamTokens.LavenderMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    entries.forEach { entry ->
                        MorCamGlassCard(
                            cornerRadius = 14.dp,
                            highlighted = entry.isConstant && entry.isEnabled,
                            paddingHorizontal = 12.dp,
                            paddingVertical = 10.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { editingEntry = entry },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = entry.title.ifBlank { "İsimsiz Madde" },
                                            fontFamily = DisplaySerifFontFamily,
                                            color = if (entry.isEnabled) Color.White else MorCamTokens.LavenderMuted,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .background(if (entry.isConstant) Color(0x44A855F7) else Color(0x3338BDF8))
                                                .padding(horizontal = 7.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = if (entry.isConstant) "SABİT • P${entry.priority}" else "P${entry.priority}",
                                                fontFamily = InterFontFamily,
                                                color = Color.White,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    if (entry.keywords.isNotBlank()) {
                                        Text(
                                            text = "Anahtarlar: ${entry.keywords}",
                                            fontFamily = InterFontFamily,
                                            color = Color(0xFFD8B4FE),
                                            fontSize = 10.5.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(Modifier.height(3.dp))
                                    Text(
                                        text = entry.content,
                                        fontFamily = InterFontFamily,
                                        color = MorCamTokens.LavenderLight,
                                        fontSize = 11.5.sp,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x28F87171))
                                        .clickable { viewModel.deleteLorebookEntry(entry.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Maddeyi Sil",
                                        tint = Color(0xFFFCA5A5),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── CREATE LOREBOOK DIALOG ──
    if (showCreateDialog) {
        MorCamDialogShell(
            onDismissRequest = { showCreateDialog = false },
            title = "Yeni Evren Ansiklopedisi",
            subtitle = "Birden fazla RP sohbetinde ortak kullanılabilir",
            icon = Icons.Default.AutoStories
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LorebookLabeledField(
                    value = newLbName,
                    onValueChange = { newLbName = it },
                    label = "Ansiklopedi Adı",
                    placeholder = "Örn: Nevermore & Jericho Evreni",
                    singleLine = true
                )
                LorebookLabeledField(
                    value = newLbDesc,
                    onValueChange = { newLbDesc = it },
                    label = "Kısa Açıklama",
                    placeholder = "Evrenin temel kapsamı...",
                    singleLine = false,
                    minHeight = 76.dp
                )
                MorCamCapsuleButton(
                    text = "Oluştur ve Bu Bota Bağla",
                    onClick = {
                        val trimmed = newLbName.trim()
                        if (trimmed.isNotEmpty()) {
                            val newLb = LorebookEntity(
                                name = trimmed,
                                description = newLbDesc.trim(),
                                tokenBudget = 750
                            )
                            viewModel.upsertLorebook(newLb)
                            viewModel.setLorebookLinkedToBot(botId, newLb.id, true)
                            selectedLorebookId = newLb.id
                            showCreateDialog = false
                        }
                    },
                    variant = MorCamButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // ── IMPORT JSON DIALOG ──
    if (showImportDialog) {
        MorCamDialogShell(
            onDismissRequest = { showImportDialog = false },
            title = "Lorebook JSON İçe Aktar",
            subtitle = "Velora veya SillyTavern / CharacterAI Lorebook JSON yapıştır",
            icon = Icons.Default.FileUpload
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LorebookLabeledField(
                    value = importJsonText,
                    onValueChange = { importJsonText = it },
                    label = "Lorebook JSON",
                    placeholder = "{\"name\": \"...\", \"entries\": [...]}",
                    singleLine = false,
                    minHeight = 132.dp
                )
                MorCamCapsuleButton(
                    text = "İçe Aktar ve Bağla",
                    onClick = {
                        scope.launch {
                            try {
                                val imported = viewModel.importLorebookFromJson(importJsonText, linkToBotId = botId)
                                selectedLorebookId = imported.id
                                showImportDialog = false
                                Toast.makeText(context, "\"${imported.name}\" içe aktarıldı ✦", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "JSON hatası: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    variant = MorCamButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // ── ADD / EDIT ENTRY DIALOG ──
    val currentEntry = editingEntry
    if (currentEntry != null) {
        var eTitle by remember(currentEntry.id) { mutableStateOf(currentEntry.title) }
        var eKeywords by remember(currentEntry.id) { mutableStateOf(currentEntry.keywords) }
        var eContent by remember(currentEntry.id) { mutableStateOf(currentEntry.content) }
        var eCategory by remember(currentEntry.id) { mutableStateOf(currentEntry.category) }
        var ePriority by remember(currentEntry.id) { mutableFloatStateOf(currentEntry.priority.toFloat()) }
        var eConstant by remember(currentEntry.id) { mutableStateOf(currentEntry.isConstant) }
        var eEnabled by remember(currentEntry.id) { mutableStateOf(currentEntry.isEnabled) }

        MorCamDialogShell(
            onDismissRequest = { editingEntry = null },
            title = if (currentEntry.title.isBlank()) "Yeni Lorebook Maddesi" else "Maddeyi Düzenle",
            subtitle = "Anahtar kelimeler Türkçe eklerle de uyumlu taranır",
            icon = Icons.Default.EditNote
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LorebookLabeledField(
                    value = eTitle,
                    onValueChange = { eTitle = it },
                    label = "Başlık",
                    placeholder = "Örn: Galatasaray Aile Kolyesi",
                    singleLine = true
                )
                LorebookLabeledField(
                    value = eKeywords,
                    onValueChange = { eKeywords = it },
                    label = "Tetikleyici Anahtar Kelimeler (virgülle ayır)",
                    placeholder = "Örn: kolye, galatasaray, amblem, enerji",
                    singleLine = true
                )

                // Category Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LorebookManager.CATEGORIES.forEach { (key, label) ->
                        MorCamChipPill(
                            label = label,
                            selected = eCategory == key,
                            onClick = { eCategory = key }
                        )
                    }
                }

                LorebookLabeledField(
                    value = eContent,
                    onValueChange = { eContent = it },
                    label = "Ansiklopedi İçeriği (Prompt'a Enjekte Edilecek Bilgi)",
                    placeholder = "Kanonik bilgi, kural, mekan veya karakter detayı...",
                    singleLine = false,
                    minHeight = 112.dp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Her Zaman Aktif (Sabit)",
                            fontFamily = LiterarySerifFontFamily,
                            color = Color.White,
                            fontSize = 12.5.sp
                        )
                        Text(
                            text = "Anahtar kelime beklemeden her RP turunda dahil edilir.",
                            fontFamily = InterFontFamily,
                            color = MorCamTokens.LavenderMuted,
                            fontSize = 10.5.sp
                        )
                    }
                    Switch(
                        checked = eConstant,
                        onCheckedChange = { eConstant = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF9333EA))
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Öncelik: ${ePriority.toInt()} / 10",
                        fontFamily = LiterarySerifFontFamily,
                        color = MorCamTokens.SilverLavenderBright,
                        fontSize = 12.sp
                    )
                    Switch(
                        checked = eEnabled,
                        onCheckedChange = { eEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF10B981))
                    )
                }
                Slider(
                    value = ePriority,
                    onValueChange = { ePriority = it },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFD8B4FE),
                        activeTrackColor = Color(0xFFA855F7)
                    )
                )

                MorCamCapsuleButton(
                    text = "Maddeyi Kaydet",
                    onClick = {
                        if (eTitle.isNotBlank() && eContent.isNotBlank()) {
                            viewModel.upsertLorebookEntry(
                                currentEntry.copy(
                                    title = eTitle.trim(),
                                    keywords = eKeywords.trim(),
                                    content = eContent.trim(),
                                    category = eCategory,
                                    priority = ePriority.toInt().coerceIn(1, 10),
                                    isConstant = eConstant,
                                    isEnabled = eEnabled
                                )
                            )
                            editingEntry = null
                        } else {
                            Toast.makeText(context, "Başlık ve içerik boş bırakılamaz.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    variant = MorCamButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun LorebookLabeledField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    singleLine: Boolean = true,
    minHeight: androidx.compose.ui.unit.Dp = 48.dp,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            fontFamily = LiterarySerifFontFamily,
            color = MorCamTokens.LavenderLight,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium
        )
        MorCamGlassTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            singleLine = singleLine,
            minHeight = minHeight,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
