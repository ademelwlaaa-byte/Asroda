package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MessageEntity
import com.example.util.ChatStoryStats
import com.example.util.StoryBookmarkStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun ChatStoryStatsDialog(
    context: Context,
    bot: com.example.data.local.BotEntity,
    displayName: String,
    validMessages: List<MessageEntity>,
    onDismiss: () -> Unit
) {
    val stats = remember(validMessages) { ChatStoryStats.compute(validMessages) }
    val atmosphere = remember(validMessages) { com.example.util.StoryMoodAnalytics.detectDominantAtmosphere(validMessages) }
    val pacing = remember(validMessages) { com.example.util.StoryPacingAnalyzer.analyze(validMessages) }
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("tr-TR")) }
    MorCamDialogShell(
        onDismissRequest = onDismiss,
        title = "Hikaye İstatistikleri",
        subtitle = "$displayName • Roman Özeti"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val estTokens = com.example.util.TokenBudgetEstimator.estimateTokens(stats.totalWordCount)
            val fillPct = com.example.util.TokenBudgetEstimator.contextFillPercent(estTokens)
            listOf(
                "Baskın Atmosfer" to atmosphere,
                "Anlatım Dengesi" to com.example.util.StoryPacingAnalyzer.formatCompact(pacing),
                "Toplam Mesaj" to "${stats.totalMessages} (Sen: ${stats.userMessages} • $displayName: ${stats.assistantMessages})",
                "Toplam Kelime" to "${stats.totalWordCount} kelime",
                "Kelime Dengesi" to "Sen: ${stats.userWordCount} • $displayName: ${stats.assistantWordCount}",
                "Tahmini Token" to "${com.example.util.TokenBudgetEstimator.formatTokenCount(estTokens)} (%$fillPct / 128K)",
                "Tahmini Roman Sayfası" to "~${stats.estimatedNovelPages} sayfa",
                "Tahmini Okuma Süresi" to "~${stats.estimatedReadingMinutes} dakika",
                "Hikaye Aralığı" to if (stats.firstTimestampMs > 0L) {
                    "${dateFmt.format(Date(stats.firstTimestampMs))} — ${dateFmt.format(Date(stats.lastTimestampMs))}"
                } else "Henüz başlamadı"
            ).forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        color = Color(0xFFC4B5FD),
                        fontSize = 12.5.sp
                    )
                    Text(
                        text = value,
                        color = Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            MorCamCapsuleButton(
                text = "📋 Karakter Kartını Kopyala",
                variant = MorCamButtonVariant.Secondary,
                onClick = {
                    val cardMd = com.example.util.BotProfileExporter.formatCharacterCardMarkdown(bot)
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                    cm?.setPrimaryClip(android.content.ClipData.newPlainText(displayName, cardMd))
                    Toast.makeText(context, "Karakter kartı panoya kopyalandı", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
internal fun ChatStoryBookmarksDialog(
    context: Context,
    botId: String,
    displayName: String,
    validMessages: List<MessageEntity>,
    storyBookmarks: List<StoryBookmarkStore.Bookmark>,
    onBookmarksChanged: (List<StoryBookmarkStore.Bookmark>) -> Unit,
    onJumpToMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var bookmarkTitle by remember { mutableStateOf("") }
    val latestMsg = validMessages.lastOrNull()
    MorCamDialogShell(
        onDismissRequest = onDismiss,
        title = "Hikaye Yer İmleri",
        subtitle = "$displayName • Önemli sahnelere anında dön"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (latestMsg != null) {
                OutlinedTextField(
                    value = bookmarkTitle,
                    onValueChange = { bookmarkTitle = it },
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = "Yer imi adı (örn. Kulede ihanet)",
                            color = Color(0x99C4B5FD),
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                MorCamCapsuleButton(
                    text = "Son Sahneyi Yer İmi Olarak Kaydet",
                    onClick = {
                        val label = bookmarkTitle.trim().ifBlank { "Sahne #${validMessages.size}" }
                        val updated = StoryBookmarkStore.addBookmark(
                            context = context,
                            botId = botId,
                            messageId = latestMsg.id,
                            title = label,
                            snippet = latestMsg.text
                        )
                        onBookmarksChanged(updated)
                        bookmarkTitle = ""
                        Toast.makeText(context, "Yer imi kaydedildi: $label", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (storyBookmarks.isEmpty()) {
                Text(
                    text = "Henüz kayıtlı yer imi yok. Önemli sahneleri işaretleyip tek dokunuşla geri dönebilirsin.",
                    color = Color(0xFFC4B5FD),
                    fontSize = 12.sp
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    storyBookmarks.forEach { bm ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x441B0D36))
                                .border(0.8.dp, Color(0x55C084FC), RoundedCornerShape(12.dp))
                                .clickable {
                                    onDismiss()
                                    onJumpToMessage(bm.messageId)
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🔖 ${bm.title}",
                                    color = Color.White,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                                if (bm.snippet.isNotBlank()) {
                                    Text(
                                        text = bm.snippet,
                                        color = Color(0xFFC4B5FD),
                                        fontSize = 11.sp,
                                        maxLines = 2
                                    )
                                }
                            }
                            Text(
                                text = "Sil",
                                color = Color(0xFFFCA5A5),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clickable {
                                        onBookmarksChanged(
                                            StoryBookmarkStore.removeBookmark(
                                                context = context,
                                                botId = botId,
                                                bookmarkId = bm.id
                                            )
                                        )
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ChatStoryDirectorDialog(
    context: Context,
    botId: String,
    displayName: String,
    lastAssistantText: String = "",
    currentResponseLength: String,
    currentTtsSpeed: Float,
    currentFontScale: Float,
    onSelectResponseLength: (String) -> Unit,
    onSelectTtsSpeed: (Float) -> Unit,
    onSelectFontScale: (Float) -> Unit,
    onInsertMacro: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var macros by remember { mutableStateOf(com.example.util.RoleplayMacroStore.loadMacros(context)) }
    var directorRule by remember(botId) {
        mutableStateOf(com.example.util.BotDirectorNoteStore.loadRule(context, botId))
    }
    MorCamDialogShell(
        onDismissRequest = onDismiss,
        title = "Hikaye Yönetmeni",
        subtitle = "$displayName • Uzunluk, ses, yazı boyutu & makrolar"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Yanıt Uzunluğu",
                color = Color(0xFFC4B5FD),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            val lengths = listOf(
                "short" to "Kısa",
                "standard" to "Dengeli",
                "detailed" to "Detaylı",
                "novel" to "Roman"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                lengths.forEach { (key, label) ->
                    val selected = currentResponseLength.equals(key, ignoreCase = true)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) Color(0xCC7C3AED) else Color(0x551B0D36))
                            .border(
                                0.9.dp,
                                if (selected) Color(0xFFE9D5FF) else Color(0x55B794F4),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectResponseLength(key) }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Text(
                text = "Sesli Okuma Hızı",
                color = Color(0xFFC4B5FD),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            val speeds = listOf(0.85f to "0.85x", 1.0f to "1.0x", 1.15f to "1.15x", 1.3f to "1.3x")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                speeds.forEach { (spd, label) ->
                    val selected = kotlin.math.abs(currentTtsSpeed - spd) < 0.05f
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) Color(0xAA059669) else Color(0x551B0D36))
                            .border(
                                0.9.dp,
                                if (selected) Color(0xFFA7F3D0) else Color(0x55B794F4),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectTtsSpeed(spd) }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Text(
                text = "Sohbet Yazı Boyutu",
                color = Color(0xFFC4B5FD),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            val fontScales = listOf(0.92f to "Küçük", 1.0f to "Normal", 1.12f to "Rahat", 1.24f to "Büyük")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                fontScales.forEach { (sc, label) ->
                    val selected = kotlin.math.abs(currentFontScale - sc) < 0.04f
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) Color(0xCC7C3AED) else Color(0x551B0D36))
                            .border(
                                0.9.dp,
                                if (selected) Color(0xFFE9D5FF) else Color(0x55B794F4),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectFontScale(sc) }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Text(
                text = "Özel Yönetmen Kuralı (Her Yanıtta AI'ya İletilir)",
                color = Color(0xFFC4B5FD),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = directorRule,
                onValueChange = { directorRule = it },
                minLines = 2,
                maxLines = 4,
                placeholder = {
                    Text(
                        text = "Ör. Asla benim yerime konuşma, 3. şahıs edebi anlatım kullan...",
                        color = Color(0x99C4B5FD),
                        fontSize = 11.5.sp
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            MorCamCapsuleButton(
                text = "Yönetmen Kuralını Kaydet",
                onClick = {
                    com.example.util.BotDirectorNoteStore.saveRule(context, botId, directorRule)
                    Toast.makeText(
                        context,
                        if (directorRule.isBlank()) "Yönetmen kuralı temizlendi" else "Yönetmen kuralı kaydedildi",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            )

            val smartStarters = remember(lastAssistantText) {
                com.example.util.QuickReplyChipsHelper.suggestStarters(lastAssistantText)
            }
            Text(
                text = "Sahneye Göre Hızlı Başlangıçlar (Dokun & Ekle)",
                color = Color(0xFFC4B5FD),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                smartStarters.forEach { starter ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x44211044))
                            .border(0.8.dp, Color(0x55A78BFA), RoundedCornerShape(10.dp))
                            .clickable {
                                onInsertMacro(starter)
                                onDismiss()
                            }
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "✦ $starter",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            maxLines = 2
                        )
                    }
                }
            }

            Text(
                text = "Hızlı Rol Yapma Makroları (Dokun & Ekle)",
                color = Color(0xFFC4B5FD),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            var newMacroLabel by remember { mutableStateOf("") }
            var newMacroText by remember { mutableStateOf("") }
            OutlinedTextField(
                value = newMacroLabel,
                onValueChange = { newMacroLabel = it },
                singleLine = true,
                placeholder = { Text("Yeni makro adı (örn. Savunma)", color = Color(0x99C4B5FD), fontSize = 11.5.sp) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = newMacroText,
                onValueChange = { newMacroText = it },
                singleLine = true,
                placeholder = { Text("Makro metni (örn. *Kılıcını çekip bekler.*)", color = Color(0x99C4B5FD), fontSize = 11.5.sp) },
                modifier = Modifier.fillMaxWidth()
            )
            if (newMacroLabel.isNotBlank() && newMacroText.isNotBlank()) {
                MorCamCapsuleButton(
                    text = "+ Makro Ekle",
                    onClick = {
                        macros = com.example.util.RoleplayMacroStore.addMacro(context, newMacroLabel, newMacroText)
                        newMacroLabel = ""
                        newMacroText = ""
                        Toast.makeText(context, "Özel makro kaydedildi", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                macros.forEach { macro ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x441B0D36))
                            .border(0.8.dp, Color(0x55C084FC), RoundedCornerShape(10.dp))
                            .clickable {
                                onInsertMacro(macro.text)
                                onDismiss()
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⚡ ${macro.label}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = macro.text,
                                color = Color(0xFFC4B5FD),
                                fontSize = 10.5.sp,
                                maxLines = 1
                            )
                        }
                        if (!macro.id.startsWith("def_")) {
                            Text(
                                text = "Sil",
                                color = Color(0xFFFCA5A5),
                                fontSize = 10.5.sp,
                                modifier = Modifier
                                    .clickable {
                                        macros = com.example.util.RoleplayMacroStore.removeMacro(context, macro.id)
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ChatScratchpadDialog(
    context: Context,
    botId: String,
    displayName: String,
    onDismiss: () -> Unit
) {
    var noteText by remember(botId) {
        mutableStateOf(com.example.util.BotScratchpadStore.loadNote(context, botId))
    }
    MorCamDialogShell(
        onDismissRequest = {
            com.example.util.BotScratchpadStore.saveNote(context, botId, noteText)
            onDismiss()
        },
        title = "Hikaye Not Defteri",
        subtitle = "$displayName • Sadece senin görebileceğin özel notlar"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                minLines = 5,
                maxLines = 10,
                placeholder = {
                    Text(
                        text = "İpuçları, yan karakter adları, envanter veya planlarını buraya yaz...",
                        color = Color(0x99C4B5FD),
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MorCamCapsuleButton(
                    text = "Temizle",
                    variant = MorCamButtonVariant.Secondary,
                    onClick = {
                        noteText = ""
                        com.example.util.BotScratchpadStore.clearNote(context, botId)
                        Toast.makeText(context, "Not defteri temizlendi", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                )
                MorCamCapsuleButton(
                    text = "Kaydet",
                    onClick = {
                        com.example.util.BotScratchpadStore.saveNote(context, botId, noteText)
                        Toast.makeText(context, "Notlar kaydedildi", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1.4f)
                )
            }
        }
    }
}
