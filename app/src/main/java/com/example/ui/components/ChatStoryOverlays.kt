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
    val cadence = remember(validMessages) { com.example.util.StoryTurnCadenceAnalyzer.analyze(validMessages) }
    val exprCounts = remember(validMessages) { com.example.util.StoryDialogueQuoteCounter.count(validMessages) }
    val lastAiTone = remember(validMessages) {
        val lastAi = validMessages.lastOrNull { it.role == "assistant" }?.text.orEmpty()
        com.example.util.CharacterDialogueToneDetector.detectTone(lastAi)
    }
    val proseMetrics = remember(validMessages) {
        com.example.util.StoryReadingLevelEstimator.estimate(validMessages)
    }
    val promptBudget = remember(bot) {
        com.example.util.BotPromptBudgetEstimator.estimate(bot)
    }
    val vocabRichness = remember(validMessages) {
        com.example.util.StoryVocabularyRichnessHelper.analyze(validMessages)
    }
    val genreTags = remember(bot) {
        com.example.util.StoryGenreTagClassifier.inferTags(bot)
    }
    val readability = remember(validMessages) {
        com.example.util.StoryParagraphReadabilityHelper.evaluate(validMessages)
    }
    val bestQuote = remember(validMessages, displayName) {
        com.example.util.StoryQuoteExtractor.extractBestQuote(validMessages, displayName)
    }
    val topThemes = remember(validMessages) {
        com.example.util.CharacterWordCloudHelper.extractTopThemes(validMessages, maxThemes = 5)
    }
    val castMentions = remember(validMessages, bot.keyCharactersJson) {
        val names = com.example.util.CharacterMentionTracker.extractCastNames(bot.keyCharactersJson)
        com.example.util.CharacterMentionTracker.countMentions(validMessages, names, maxResults = 4)
    }
    val estAudioText = remember(stats.totalWordCount) {
        val secs = com.example.util.TtsSpeechEstimator.estimateSeconds(stats.totalWordCount)
        com.example.util.TtsSpeechEstimator.formatDurationTurkish(secs)
    }
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
                "Hikaye Seviyesi" to com.example.util.StoryDepthTierClassifier.formatLabel(stats.totalWordCount),
                "Türler" to genreTags.joinToString(" • "),
                "Baskın Atmosfer" to atmosphere,
                "Anlatım Dengesi" to com.example.util.StoryPacingAnalyzer.formatCompact(pacing),
                "Diyalog & Aksiyon" to com.example.util.StoryDialogueQuoteCounter.formatCompact(exprCounts),
                "Son Sahne Tonu" to lastAiTone,
                "Edebi Üslup" to com.example.util.StoryReadingLevelEstimator.formatCompact(proseMetrics),
                "Okuma Akıcılığı" to readability.label,
                "Kelime Zenginliği" to com.example.util.StoryVocabularyRichnessHelper.formatCompact(vocabRichness),
                "Sahne Ritmi" to com.example.util.StoryTurnCadenceAnalyzer.formatCompact(cadence),
                "Toplam Mesaj" to "${stats.totalMessages} (Sen: ${stats.userMessages} • $displayName: ${stats.assistantMessages})",
                "Toplam Kelime" to "${stats.totalWordCount} kelime",
                "Kelime Dengesi" to "Sen: ${stats.userWordCount} • $displayName: ${stats.assistantWordCount}",
                "Tahmini Token" to "${com.example.util.TokenBudgetEstimator.formatTokenCount(estTokens)} (%$fillPct / 128K)",
                "Karakter Kartı Yükü" to com.example.util.BotPromptBudgetEstimator.formatCompact(promptBudget),
                "Tahmini Roman Sayfası" to "~${stats.estimatedNovelPages} sayfa",
                "Tahmini Okuma Süresi" to "~${stats.estimatedReadingMinutes} dakika",
                "Tahmini Sesli Dinleme" to estAudioText,
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
            if (castMentions.isNotEmpty()) {
                Text(
                    text = "Sahnedeki Karakterler: ${castMentions.joinToString(" • ") { "${it.name} (${it.count})" }}",
                    color = Color(0xFFD8B4FE),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            if (topThemes.isNotEmpty()) {
                Text(
                    text = "Öne Çıkan Temalar: ${topThemes.joinToString("  ")}",
                    color = Color(0xFFC4B5FD),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            if (bestQuote != null) {
                Text(
                    text = "✨ $bestQuote",
                    color = Color(0xFFE9D5FF),
                    fontSize = 11.5.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x441B0D36))
                        .border(0.7.dp, Color(0x55C084FC), RoundedCornerShape(10.dp))
                        .clickable {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                            cm?.setPrimaryClip(android.content.ClipData.newPlainText(displayName, bestQuote))
                            Toast.makeText(context, "Replik panoya kopyalandı", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MorCamCapsuleButton(
                    text = "📖 Bölüm Özeti",
                    variant = MorCamButtonVariant.Primary,
                    onClick = {
                        val recapMd = com.example.util.ChapterSummaryExporter.formatRecapMarkdown(
                            title = displayName,
                            userName = bot.userCharName.ifBlank { "Sen" },
                            messages = validMessages
                        )
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                        cm?.setPrimaryClip(android.content.ClipData.newPlainText(displayName, recapMd))
                        Toast.makeText(context, "Bölüm özeti panoya kopyalandı", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                )
                MorCamCapsuleButton(
                    text = "📋 Karakter Kartı",
                    variant = MorCamButtonVariant.Secondary,
                    onClick = {
                        val cardMd = com.example.util.BotProfileExporter.formatCharacterCardMarkdown(bot)
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                        cm?.setPrimaryClip(android.content.ClipData.newPlainText(displayName, cardMd))
                        Toast.makeText(context, "Karakter kartı panoya kopyalandı", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            MorCamCapsuleButton(
                text = "📦 Tam Hikaye Arşivini Kopyala",
                variant = MorCamButtonVariant.Secondary,
                onClick = {
                    val bms = StoryBookmarkStore.loadBookmarks(context, bot.id)
                    val archive = com.example.util.StoryArchiveBundler.buildCompleteArchive(bot, validMessages, bms)
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                    cm?.setPrimaryClip(android.content.ClipData.newPlainText(displayName, archive))
                    Toast.makeText(context, "Tam hikaye arşivi panoya kopyalandı", Toast.LENGTH_SHORT).show()
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
    var bookmarkSearch by remember { mutableStateOf("") }
    val filteredBookmarks = remember(storyBookmarks, bookmarkSearch) {
        com.example.util.StoryBookmarkSearchHelper.filter(storyBookmarks, bookmarkSearch)
    }
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
                        val label = bookmarkTitle.trim().ifBlank {
                            com.example.util.StorySceneTitleGenerator.suggestTitle(latestMsg.text, validMessages.size)
                        }
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
                MorCamCapsuleButton(
                    text = "📋 Yer İmlerini Kopyala (${storyBookmarks.size})",
                    variant = MorCamButtonVariant.Secondary,
                    onClick = {
                        val md = com.example.util.StoryBookmarkExporter.formatMarkdown(displayName, storyBookmarks)
                        if (md.isNotBlank()) {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                            cm?.setPrimaryClip(android.content.ClipData.newPlainText(displayName, md))
                            Toast.makeText(context, "Yer imleri panoya kopyalandı", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                if (storyBookmarks.size >= 4) {
                    OutlinedTextField(
                        value = bookmarkSearch,
                        onValueChange = { bookmarkSearch = it },
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = "Yer imlerinde ara...",
                                color = Color(0x99C4B5FD),
                                fontSize = 11.5.sp
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filteredBookmarks.forEach { bm ->
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
                                val relTime = com.example.util.MessageTimestampFormatter.formatRelative(bm.createdAtMs)
                                Text(
                                    text = if (relTime.isNotBlank()) "🔖 ${bm.title} • $relTime" else "🔖 ${bm.title}",
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
        subtitle = com.example.util.StoryDirectorSummaryHelper.formatActiveConfig(
            responseLength = currentResponseLength,
            ttsSpeed = currentTtsSpeed,
            fontScale = currentFontScale,
            hasDirectorRule = directorRule.isNotBlank()
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Yanıt Uzunluğu • ${com.example.util.ResponseLengthTargetHelper.describeTarget(currentResponseLength)}",
                color = Color(0xFFC4B5FD),
                fontSize = 11.5.sp,
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
                text = "Sesli Okuma Hızı • ${com.example.util.TtsSpeedLabelFormatter.formatLabel(currentTtsSpeed)}",
                color = Color(0xFFC4B5FD),
                fontSize = 11.5.sp,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                com.example.util.StoryTonePresets.presets.forEach { preset ->
                    val active = directorRule.trim() == preset.directive
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (active) Color(0xAA7C3AED) else Color(0x441B0D36))
                            .border(
                                0.8.dp,
                                if (active) Color(0xFFE9D5FF) else Color(0x55B794F4),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                directorRule = preset.directive
                                com.example.util.BotDirectorNoteStore.saveRule(context, botId, preset.directive)
                                Toast.makeText(context, "${preset.label} tonu uygulandı", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 6.dp, horizontal = 3.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = preset.label,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
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
                text = "Hızlı Rol Yapma Biçimleri (Dokun & Ekle)",
                color = Color(0xFFC4B5FD),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "*Aksiyon*" to com.example.util.ChatInputQuickSyntaxHelper.wrapAction("..."),
                    "\"Diyalog\"" to com.example.util.ChatInputQuickSyntaxHelper.wrapDialogue("..."),
                    "[OOC]" to com.example.util.ChatInputQuickSyntaxHelper.wrapOoc("...")
                ).forEach { (chipLabel, snippet) ->
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x44211044))
                            .border(0.8.dp, Color(0x55C084FC), RoundedCornerShape(10.dp))
                            .clickable {
                                onInsertMacro(snippet)
                                onDismiss()
                            }
                            .padding(vertical = 7.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = chipLabel,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
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
            Text(
                text = com.example.util.TextWordCounter.formatCompact(noteText),
                color = Color(0xFFC4B5FD),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            if (noteText.isNotBlank()) {
                MorCamCapsuleButton(
                    text = "📋 Notu Panoya Kopyala",
                    variant = MorCamButtonVariant.Secondary,
                    onClick = {
                        val formatted = "# $displayName — Hikaye Notları\n\n${noteText.trim()}"
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                        cm?.setPrimaryClip(android.content.ClipData.newPlainText(displayName, formatted))
                        Toast.makeText(context, "Not panoya kopyalandı", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
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
