@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.BotEntity
import com.example.ui.animation.cosmicFloatingLevitation
import com.example.ui.animation.cosmicStaggeredEnter
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LiterarySerifFontFamily

@Composable
fun NeuralVaultModal(
    bot: BotEntity,
    viewModel: com.example.ui.viewmodel.EmochiViewModel? = null,
    onDismiss: () -> Unit,
    onSaveMemory: (BotEntity) -> Unit
) {
    var keyword by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Bilgi (Core)") }
    var memoryText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var categoryFilter by remember { mutableStateOf("Tümü") }

    var pinnedMemoryText by remember { mutableStateOf(bot.pinnedMemory) }
    var memoryNotesText by remember { mutableStateOf(bot.memoryNotes) }
    var storyNotesText by remember { mutableStateOf(bot.storyNotes) }

    val activeFactsFlow = remember(bot.id) { viewModel?.getActiveFactsFlow(bot.id) }
    val activeFacts by (activeFactsFlow ?: kotlinx.coroutines.flow.flowOf(emptyList())).collectAsStateWithLifecycle(initialValue = emptyList())

    val activeEventsFlow = remember(bot.id) { viewModel?.getActiveEventsFlow(bot.id) }
    val activeEvents by (activeEventsFlow ?: kotlinx.coroutines.flow.flowOf(emptyList())).collectAsStateWithLifecycle(initialValue = emptyList())

    var editingFact by remember { mutableStateOf<com.example.data.local.MemoryFactEntity?>(null) }
    var editingEvent by remember { mutableStateOf<com.example.data.local.MemoryEventEntity?>(null) }

    val memoriesList = remember(pinnedMemoryText, memoryNotesText, storyNotesText) {
        val list = mutableListOf<Triple<String, String, String>>()

        if (pinnedMemoryText.isNotBlank()) {
            pinnedMemoryText.lines().filter { it.isNotBlank() }.forEach { line ->
                val parts = line.split(":::", limit = 3)
                if (parts.size >= 3) {
                    list.add(Triple(parts[0].trim(), parts[1].trim(), parts[2].trim()))
                } else if (parts.size == 2) {
                    list.add(Triple("BELLEK", "KALICI", parts[1].trim()))
                } else {
                    list.add(Triple("KALICI", "BİLGİ", line.trim()))
                }
            }
        }

        if (memoryNotesText.isNotBlank()) {
            memoryNotesText.lines().filter { it.isNotBlank() }.forEach { line ->
                val clean = line.removePrefix("-").removePrefix("*").trim()
                if (clean.isNotBlank()) {
                    list.add(Triple("ÖZET", "HAFIZA", clean))
                }
            }
        }

        if (storyNotesText.isNotBlank()) {
            storyNotesText.lines().filter { it.isNotBlank() }.forEach { line ->
                val clean = line.removePrefix("-").removePrefix("*").trim()
                if (clean.isNotBlank()) {
                    list.add(Triple("HİKAYE", "DURUM", clean))
                }
            }
        }

        list
    }

    val filteredMemories = memoriesList.filter { (key, cat, body) ->
        val matchesCategory = categoryFilter == "Tümü" ||
            categoryFilter == "Sabit" ||
            cat.contains(categoryFilter, ignoreCase = true) ||
            (categoryFilter == "Bilgi (Core)" && (cat.contains("BİLGİ", ignoreCase = true) || cat.contains("CORE", ignoreCase = true)))
        val matchesSearch = searchQuery.isBlank() ||
            key.contains(searchQuery, ignoreCase = true) ||
            cat.contains(searchQuery, ignoreCase = true) ||
            body.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    val filteredFacts = activeFacts.filter { fact ->
        val matchesCategory = categoryFilter == "Tümü" ||
            categoryFilter == "Otomatik Bilgiler" ||
            categoryFilter.contains("Bilgi", ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
            fact.subject.contains(searchQuery, ignoreCase = true) ||
            fact.key.contains(searchQuery, ignoreCase = true) ||
            fact.value.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    val filteredEvents = activeEvents.filter { event ->
        val matchesCategory = categoryFilter == "Tümü" ||
            categoryFilter == "Anılar" ||
            categoryFilter.contains("Biyografi", ignoreCase = true) ||
            categoryFilter.contains("İlişki", ignoreCase = true)
        val searchable = com.example.data.repository.LivingMemoryCodec.toSearchableText(event.description)
        val matchesSearch = searchQuery.isBlank() || searchable.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    val totalSaved = memoriesList.size + activeFacts.size + activeEvents.size

    MorCamDialogShell(
        onDismissRequest = onDismiss,
        title = "Neural Vault • Bellek Mahzeni",
        subtitle = "$totalSaved kayıt • Hibrit RAG & Çoklu Bakış Açılı Anılar",
        icon = Icons.Default.Memory,
        maxWidthFraction = 0.94f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 580.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats Badges Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MorCamChipPill(label = "$totalSaved Kayıt", selected = true, onClick = {})
                MorCamChipPill(label = "V4.0 Sync", selected = false, onClick = {})
                MorCamChipPill(label = "Uzun Dönem Bellek", selected = false, onClick = {})
            }

            // Inject Memory Glass Card
            MorCamGlassCard(
                cornerRadius = 18.dp,
                highlighted = true,
                paddingHorizontal = 14.dp,
                paddingVertical = 14.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MorCamTokens.LavenderLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "YENİ BELLEK ENJEKTE ET",
                        fontFamily = InterFontFamily,
                        color = MorCamTokens.LavenderLight,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MorCamGlassTextField(
                        value = keyword,
                        onValueChange = { keyword = it },
                        placeholder = "Anahtar Kelime",
                        singleLine = true,
                        minHeight = 46.dp,
                        modifier = Modifier.weight(1f)
                    )

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 46.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Brush.verticalGradient(listOf(MorCamTokens.FieldSurfaceTop, MorCamTokens.FieldSurfaceBottom)))
                            .border(BorderStroke(0.95.dp, MorCamTokens.SubtleRimGradient), RoundedCornerShape(24.dp))
                            .clickable { showCategoryPicker = true }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category,
                            fontFamily = InterFontFamily,
                            color = MorCamTokens.SilverLavenderBright,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Kategori Seç",
                            tint = MorCamTokens.LavenderSoft,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                val quickPresets = listOf(
                    Triple("TAKMA İSİM", "Bilgi (Core)", "Kullanıcıya özel samimi bir unvan veya takma isimle hitap eder."),
                    Triple("GİZLİ DUYGU", "İlişki", "Kullanıcıya karşı derin bir çekim ve gizli bir bağlılık hisseder."),
                    Triple("GEÇMİŞ SIR", "Biyografi", "Geçmişte yaşadığı kırılma noktasını sadece kullanıcıya anlatmıştır."),
                    Triple("ÖZEL MEKAN", "Dünya/Mekan", "Yalnızca ikisinin bildiği gizli ve tenha bir buluşma noktaları vardır."),
                    Triple("YETİŞKİN RP", "İlişki", "Yetişkin etkileşimlerde filtresiz, tutkulu ve tabusuz bir yakınlık benimser."),
                    Triple("SADAKAT BAĞI", "İlişki", "Kullanıcıya koşulsuz sadakat gösterir ve onu her koşulda korur.")
                )

                Text(
                    text = "HAZIR ŞABLONLAR",
                    fontFamily = InterFontFamily,
                    color = MorCamTokens.LavenderMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickPresets.forEach { (presetKey, presetCat, presetDesc) ->
                        MorCamChipPill(
                            label = presetKey,
                            selected = keyword.equals(presetKey, ignoreCase = true),
                            icon = Icons.Default.AutoAwesome,
                            onClick = {
                                keyword = presetKey
                                category = presetCat
                                memoryText = presetDesc
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                MorCamGlassTextField(
                    value = memoryText,
                    onValueChange = { memoryText = it },
                    placeholder = "Karakter neyi hatırlamalı? (Örn: En sevdiği yemek makarnadır.)",
                    singleLine = false,
                    minHeight = 82.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                MorCamCapsuleButton(
                    text = "Belleği Kaydet",
                    onClick = {
                        if (memoryText.isNotBlank()) {
                            val keyTag = keyword.ifBlank { "BELLEK" }.uppercase()
                            val catTag = category.ifBlank { "BİLGİ (CORE)" }.uppercase()
                            val newEntry = "$keyTag ::: $catTag ::: ${memoryText.trim()}"
                            val newPinned = if (pinnedMemoryText.isBlank()) newEntry else "$pinnedMemoryText\n$newEntry"
                            pinnedMemoryText = newPinned
                            onSaveMemory(bot.copy(pinnedMemory = newPinned))
                            keyword = ""
                            memoryText = ""
                        }
                    },
                    enabled = memoryText.isNotBlank(),
                    variant = MorCamButtonVariant.Primary,
                    icon = Icons.Default.Add,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Category Filter Chips
            val presetCategories = listOf("Tümü", "Anılar", "Otomatik Bilgiler", "Sabit", "İlişki", "Dünya/Mekan")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presetCategories.forEach { cat ->
                    MorCamChipPill(
                        label = cat,
                        selected = categoryFilter == cat,
                        onClick = { categoryFilter = cat }
                    )
                }
            }

            // Search Bar
            MorCamGlassTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Hafızada ara...",
                leadingIcon = Icons.Default.Search,
                trailingContent = if (searchQuery.isNotEmpty()) {
                    {
                        MorCamCircleIconButton(
                            icon = Icons.Default.Close,
                            contentDescription = "Temizle",
                            onClick = { searchQuery = "" },
                            size = 36.dp
                        )
                    }
                } else null
            )

            // Saved Memory List Cards
            val totalItemsCount = filteredMemories.size + filteredFacts.size + filteredEvents.size
            if (totalItemsCount == 0) {
                MorCamGlassCard(
                    cornerRadius = 16.dp,
                    paddingHorizontal = 16.dp,
                    paddingVertical = 22.dp,
                    modifier = Modifier.cosmicFloatingLevitation(amplitudeDp = 3.dp, durationMs = 2800)
                ) {
                    Text(
                        text = "Henüz enjekte edilmiş veya öğrenilmiş bellek yok.",
                        fontFamily = InterFontFamily,
                        color = MorCamTokens.LavenderMuted,
                        fontSize = 12.5.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                // 1. Manual Injected Memories
                filteredMemories.forEachIndexed { idx, (keyTag, catTag, bodyText) ->
                    MorCamGlassCard(
                        cornerRadius = 16.dp,
                        modifier = Modifier.cosmicStaggeredEnter(index = idx, key = "mem_${categoryFilter}_${keyTag}_$idx")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = keyTag,
                                fontFamily = LiterarySerifFontFamily,
                                color = MorCamTokens.SilverLavenderBright,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0x45A855F7))
                                    .border(0.7.dp, MorCamTokens.SubtleRimGradient, RoundedCornerShape(50))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = catTag,
                                    fontFamily = InterFontFamily,
                                    color = MorCamTokens.LavenderLight,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = bodyText,
                            fontFamily = InterFontFamily,
                            color = MorCamTokens.LavenderLight,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            MorCamCircleIconButton(
                                icon = Icons.Default.Delete,
                                contentDescription = "Sil",
                                onClick = {
                                    val lines = pinnedMemoryText.lines().toMutableList()
                                    val targetLine = "$keyTag ::: $catTag ::: $bodyText"
                                    lines.remove(targetLine)
                                    val updated = lines.joinToString("\n")
                                    pinnedMemoryText = updated
                                    onSaveMemory(bot.copy(pinnedMemory = updated))
                                },
                                size = 36.dp,
                                isDestructive = true
                            )
                        }
                    }
                }

                // 2. Automatic Neural Vault Facts
                filteredFacts.forEachIndexed { idx, fact ->
                    MorCamGlassCard(
                        cornerRadius = 16.dp,
                        modifier = Modifier.cosmicStaggeredEnter(index = idx + filteredMemories.size, key = "fact_${categoryFilter}_${fact.id}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${fact.subject} • ${fact.key}",
                                fontFamily = LiterarySerifFontFamily,
                                color = MorCamTokens.SilverLavenderBright,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (fact.userCorrected) Color(0x55A855F7) else Color(0x557C3AED))
                                    .border(0.7.dp, MorCamTokens.RimGradient, RoundedCornerShape(50))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (fact.userCorrected) "★ SABİTLENDİ" else "AI OTOMATİK",
                                    fontFamily = InterFontFamily,
                                    color = MorCamTokens.SilverLavenderBright,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = fact.value,
                            fontFamily = InterFontFamily,
                            color = MorCamTokens.LavenderLight,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MorCamCircleIconButton(
                                icon = Icons.Default.Edit,
                                contentDescription = "Düzenle",
                                onClick = { editingFact = fact },
                                size = 36.dp
                            )
                            MorCamCircleIconButton(
                                icon = Icons.Default.Delete,
                                contentDescription = "Sil",
                                onClick = { viewModel?.deleteMemoryFact(fact.id) },
                                size = 36.dp,
                                isDestructive = true
                            )
                        }
                    }
                }

                // 3. Multi-POV Living Memories ("Yaşanan Anılar")
                filteredEvents.forEachIndexed { idx, event ->
                    val living = remember(event.id, event.description) {
                        com.example.data.repository.LivingMemoryCodec.parse(event.description, event.importanceScore)
                    }
                    MorCamGlassCard(
                        cornerRadius = 16.dp,
                        modifier = Modifier.cosmicStaggeredEnter(index = idx + filteredMemories.size + filteredFacts.size, key = "evt_${categoryFilter}_${event.id}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (living.isStructured) "✦ ${living.povCharacter}'ın Gözünden" else "Yaşanan Anı #${event.id}",
                                fontFamily = InterFontFamily,
                                color = Color(0xFFD8B4FE),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0x559333EA))
                                    .border(0.7.dp, MorCamTokens.RimGradient, RoundedCornerShape(50))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (living.isStructured) {
                                        "★ ${living.salience}/10" + if (living.emotionTag.isNotBlank()) " • ${living.emotionTag}" else ""
                                    } else "ANI KAYDI",
                                    fontFamily = InterFontFamily,
                                    color = MorCamTokens.SilverLavenderBright,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        if (living.isStructured && living.title.isNotBlank()) {
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = living.title,
                                fontFamily = LiterarySerifFontFamily,
                                color = MorCamTokens.SilverLavenderBright,
                                fontSize = 13.8.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = living.narrative,
                            fontFamily = LiterarySerifFontFamily,
                            color = MorCamTokens.LavenderLight,
                            fontSize = 12.2.sp,
                            lineHeight = 17.5.sp
                        )
                        if (living.innerThought.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x2E6D28D9))
                                    .border(0.7.dp, Color(0x66C4B5FD), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = "💭 Söylenmemiş İç Düşünce: \"${living.innerThought}\"",
                                    fontFamily = LiterarySerifFontFamily,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = Color(0xFFF3E8FF),
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MorCamCircleIconButton(
                                icon = Icons.Default.Edit,
                                contentDescription = "Düzenle",
                                onClick = { editingEvent = event },
                                size = 36.dp
                            )
                            MorCamCircleIconButton(
                                icon = Icons.Default.Delete,
                                contentDescription = "Sil",
                                onClick = { viewModel?.deleteMemoryEvent(event.id) },
                                size = 36.dp,
                                isDestructive = true
                            )
                        }
                    }
                }

                if (activeFacts.isNotEmpty() || activeEvents.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    MorCamCapsuleButton(
                        text = "Otomatik Bellekleri Temizle",
                        onClick = { viewModel?.clearAllAutoMemories(bot.id) },
                        variant = MorCamButtonVariant.Destructive,
                        icon = Icons.Default.DeleteSweep,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (editingFact != null) {
        val fact = editingFact!!
        var editSubject by remember(fact) { mutableStateOf(fact.subject) }
        var editKey by remember(fact) { mutableStateOf(fact.key) }
        var editValue by remember(fact) { mutableStateOf(fact.value) }

        MorCamDialogShell(
            onDismissRequest = { editingFact = null },
            title = "Otomatik Bilgiyi Düzenle",
            subtitle = "Karakterin öğrendiği bilgi kaydını güncelle",
            icon = Icons.Default.Edit
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Konu / Özne", fontFamily = LiterarySerifFontFamily, color = MorCamTokens.LavenderSoft, fontSize = 12.sp)
                MorCamGlassTextField(
                    value = editSubject,
                    onValueChange = { editSubject = it },
                    placeholder = "Örn: Kullanıcı"
                )
                Text("Anahtar", fontFamily = LiterarySerifFontFamily, color = MorCamTokens.LavenderSoft, fontSize = 12.sp)
                MorCamGlassTextField(
                    value = editKey,
                    onValueChange = { editKey = it },
                    placeholder = "Örn: En sevdiği renk"
                )
                Text("Değer / İçerik", fontFamily = LiterarySerifFontFamily, color = MorCamTokens.LavenderSoft, fontSize = 12.sp)
                MorCamGlassTextField(
                    value = editValue,
                    onValueChange = { editValue = it },
                    placeholder = "Örn: Lavanta",
                    singleLine = false,
                    minHeight = 68.dp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MorCamCapsuleButton(
                        text = "İptal",
                        onClick = { editingFact = null },
                        variant = MorCamButtonVariant.Secondary,
                        modifier = Modifier.weight(1f)
                    )
                    MorCamCapsuleButton(
                        text = "Kaydet",
                        onClick = {
                            viewModel?.updateMemoryFact(
                                fact.copy(
                                    subject = editSubject.trim(),
                                    key = editKey.trim(),
                                    value = editValue.trim(),
                                    lastConfirmedAt = System.currentTimeMillis()
                                )
                            )
                            editingFact = null
                        },
                        variant = MorCamButtonVariant.Primary,
                        modifier = Modifier.weight(1.1f)
                    )
                }
            }
        }
    }

    if (editingEvent != null) {
        val event = editingEvent!!
        val parsed = remember(event) {
            com.example.data.repository.LivingMemoryCodec.parse(event.description, event.importanceScore)
        }
        var editPov by remember(event) { mutableStateOf(parsed.povCharacter.ifBlank { bot.aiName.ifBlank { "Karakter" } }) }
        var editTitle by remember(event) { mutableStateOf(parsed.title.ifBlank { "Unutulmaz An" }) }
        var editNarrative by remember(event) { mutableStateOf(parsed.narrative) }
        var editInner by remember(event) { mutableStateOf(parsed.innerThought) }

        MorCamDialogShell(
            onDismissRequest = { editingEvent = null },
            title = "Yaşanan Anıyı Düzenle",
            subtitle = "Anlatıcı bakış açısını, anıyı ve iç sesi güncelle",
            icon = Icons.Default.Edit
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Anlatıcı Karakter (POV)", fontFamily = LiterarySerifFontFamily, color = MorCamTokens.LavenderSoft, fontSize = 12.sp)
                MorCamGlassTextField(
                    value = editPov,
                    onValueChange = { editPov = it },
                    placeholder = "Örn: Natasha, Aiden..."
                )
                Text("Anı Başlığı", fontFamily = LiterarySerifFontFamily, color = MorCamTokens.LavenderSoft, fontSize = 12.sp)
                MorCamGlassTextField(
                    value = editTitle,
                    onValueChange = { editTitle = it },
                    placeholder = "Örn: Çatıda İlk Güven"
                )
                Text("Anı Anlatımı (Birinci Tekil Şahıs)", fontFamily = LiterarySerifFontFamily, color = MorCamTokens.LavenderSoft, fontSize = 12.sp)
                MorCamGlassTextField(
                    value = editNarrative,
                    onValueChange = { editNarrative = it },
                    placeholder = "Olayı yaşamış gibi anlat...",
                    singleLine = false,
                    minHeight = 82.dp
                )
                Text("💭 Söylenmemiş İç Düşünce", fontFamily = LiterarySerifFontFamily, color = MorCamTokens.LavenderSoft, fontSize = 12.sp)
                MorCamGlassTextField(
                    value = editInner,
                    onValueChange = { editInner = it },
                    placeholder = "Dışa vurulmamış gizli hisler...",
                    singleLine = false,
                    minHeight = 62.dp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MorCamCapsuleButton(
                        text = "İptal",
                        onClick = { editingEvent = null },
                        variant = MorCamButtonVariant.Secondary,
                        modifier = Modifier.weight(1f)
                    )
                    MorCamCapsuleButton(
                        text = "Kaydet",
                        onClick = {
                            val newEncoded = if (parsed.isStructured || editInner.isNotBlank() || editPov.isNotBlank()) {
                                com.example.data.repository.LivingMemoryCodec.encode(
                                    povCharacter = editPov.trim(),
                                    title = editTitle.trim(),
                                    emotionTag = parsed.emotionTag.ifBlank { "Derin Etki" },
                                    salience = parsed.salience.coerceIn(7, 10),
                                    narrative = editNarrative.trim(),
                                    innerThought = editInner.trim()
                                )
                            } else {
                                editNarrative.trim()
                            }
                            viewModel?.updateMemoryEvent(
                                event.copy(
                                    description = newEncoded,
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                            editingEvent = null
                        },
                        variant = MorCamButtonVariant.Primary,
                        modifier = Modifier.weight(1.1f)
                    )
                }
            }
        }
    }

    if (showCategoryPicker) {
        val pickerCategories = listOf(
            "Bilgi (Core)",
            "Biyografi",
            "İlişki",
            "Dünya/Mekan"
        )
        MorCamDialogShell(
            onDismissRequest = { showCategoryPicker = false },
            title = "Bellek Kategorisi Seç",
            subtitle = "Yeni kaydın sınıflandırmasını belirle",
            icon = Icons.Default.Memory
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                pickerCategories.forEach { cat ->
                    MorCamOptionRow(
                        title = cat,
                        selected = category == cat,
                        onClick = {
                            category = cat
                            showCategoryPicker = false
                        }
                    )
                }
            }
        }
    }
}
