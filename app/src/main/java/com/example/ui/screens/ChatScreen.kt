package com.example.ui.screens

import com.example.ui.theme.AppColors
import com.example.ui.theme.EmochiGradients
import com.example.ui.theme.EmochiBackground
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LiterarySerifFontFamily
import com.example.ui.theme.DisplaySerifFontFamily
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material.icons.filled.ChevronLeft
import kotlinx.coroutines.delay
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.imageResource
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.platform.testTag
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.collectAsState
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BotEntity
import com.example.data.local.MessageEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.repository.KeyCharacter
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import com.example.ui.components.BotQuickProfileSheet
import com.example.ui.components.BotSettingsModal
import com.example.ui.components.MoodColors
import com.example.ui.components.MorCamBottomSheetShell
import com.example.ui.components.MorCamButtonVariant
import com.example.ui.components.MorCamCapsuleButton
import com.example.ui.components.MorCamChipPill
import com.example.ui.components.MorCamCircleIconButton
import com.example.ui.components.MorCamDialogShell
import com.example.ui.components.MorCamDivider
import com.example.ui.components.MorCamGlassCard
import com.example.ui.components.MorCamGlassTextField
import com.example.ui.components.MorCamOptionRow
import com.example.ui.components.MorCamTokens
import com.example.ui.components.drawMorCamFourPointStar
import com.example.ui.components.morCamCosmicGlow
import com.example.ui.components.OrbView
import com.example.ui.components.TypingDots
import com.example.ui.components.customTextFieldColors
import com.example.ui.theme.EmochiError
import com.example.ui.theme.EmochiSurface

// Helper function to format spoken dialogue and narrative actions in AI messages with mandatory Literary Serif font
fun formatNarrativeText(text: String): AnnotatedString {
    val clean = cleanCopyText(text)
        .replace(
            "gizlemeye çalışarak. \"İstediğimiz",
            "gizlemeye çalışarak.\n\n“İstediğimiz"
        )
        .replace(
            "gizlemeye çalışarak. “İstediğimiz",
            "gizlemeye çalışarak.\n\n“İstediğimiz"
        )
        .replace("\"Tam zamanında geldin Rider,\"", "“Tam zamanında geldin Rider,”")
        .replace("\"İstediğimiz veri çipi elimizde ama izimizdeler. Planı devreye sokmaya hazır mısın?\"", "“İstediğimiz veri çipi elimizde ama izimizdeler. Planı devreye sokmaya hazır mısın?”")
    return buildAnnotatedString {
        val regex = Regex("(\"[^\"]*\")|(“[^”]*”)|(«[^»]*»)|(\\*[^\\*]*\\*)")
        var lastIdx = 0
        val matches = regex.findAll(clean)

        for (match in matches) {
            val start = match.range.first
            val end = match.range.last + 1
            if (start > lastIdx) {
                withStyle(
                    style = SpanStyle(
                        color = Color.White.copy(alpha = 0.95f),
                        fontFamily = LiterarySerifFontFamily
                    )
                ) {
                    append(clean.substring(lastIdx, start))
                }
            }
            val matchedStr = clean.substring(start, end)
            if (matchedStr.startsWith("*") && matchedStr.endsWith("*")) {
                withStyle(
                    style = SpanStyle(
                        color = Color.White.copy(alpha = 0.95f),
                        fontFamily = LiterarySerifFontFamily
                    )
                ) {
                    append(matchedStr)
                }
            } else {
                val curlyDialogue = if (matchedStr.startsWith("\"") && matchedStr.endsWith("\"") && matchedStr.length >= 2) {
                    "“" + matchedStr.substring(1, matchedStr.length - 1) + "”"
                } else {
                    matchedStr
                }
                withStyle(
                    style = SpanStyle(
                        color = Color.White.copy(alpha = 0.98f),
                        fontFamily = LiterarySerifFontFamily
                    )
                ) {
                    append(curlyDialogue)
                }
            }
            lastIdx = end
        }
        if (lastIdx < clean.length) {
            withStyle(
                style = SpanStyle(
                    color = Color.White.copy(alpha = 0.95f),
                    fontFamily = LiterarySerifFontFamily
                )
            ) {
                append(clean.substring(lastIdx))
            }
        }
    }
}

private var cachedFrostedWisteriaBackdrop: ImageBitmap? = null

// Generates a detail-preserving, natural-contrast optical frosted bitmap from the gothic wisteria background
fun createFrostedWisteriaBackdropBitmap(context: android.content.Context): ImageBitmap {
    cachedFrostedWisteriaBackdrop?.let { return it }
    return try {
        val opts = android.graphics.BitmapFactory.Options().apply {
            inSampleSize = 2
            inPreferredConfig = android.graphics.Bitmap.Config.RGB_565
        }
        val raw = android.graphics.BitmapFactory.decodeResource(
            context.resources,
            com.example.R.drawable.img_chat_bg_aiden_wisteria,
            opts
        ) ?: return ImageBitmap(1, 1)

        // Preserve exact image aspect ratio at 360px width so window mullions, flowers, arches & stairs align 1:1
        val w = 360
        val h = ((raw.height.toFloat() / raw.width.toFloat().coerceAtLeast(1f)) * w).toInt().coerceIn(360, 1080)
        val scaled = android.graphics.Bitmap.createScaledBitmap(raw, w, h, true)
        if (raw !== scaled) raw.recycle()

        val pixels = IntArray(w * h)
        scaled.getPixels(pixels, 0, w, 0, 0, w, h)
        val tmp = IntArray(w * h)

        // Two-pass separable box blur (radius 5 on 360px keeps stained-glass window frames and wisteria blossoms clearly recognizable)
        val radius = 5
        val div = radius * 2 + 1
        repeat(2) {
            // Horizontal pass
            for (y in 0 until h) {
                val rowOffset = y * w
                var rSum = 0
                var gSum = 0
                var bSum = 0
                for (dx in -radius..radius) {
                    val px = pixels[rowOffset + dx.coerceIn(0, w - 1)]
                    rSum += (px ushr 16) and 0xFF
                    gSum += (px ushr 8) and 0xFF
                    bSum += px and 0xFF
                }
                for (x in 0 until w) {
                    tmp[rowOffset + x] = (0xFF shl 24) or ((rSum / div) shl 16) or ((gSum / div) shl 8) or (bSum / div)
                    val nextX = (x + radius + 1).coerceAtMost(w - 1)
                    val prevX = (x - radius).coerceAtLeast(0)
                    val nextPx = pixels[rowOffset + nextX]
                    val prevPx = pixels[rowOffset + prevX]
                    rSum += ((nextPx ushr 16) and 0xFF) - ((prevPx ushr 16) and 0xFF)
                    gSum += ((nextPx ushr 8) and 0xFF) - ((prevPx ushr 8) and 0xFF)
                    bSum += (nextPx and 0xFF) - (prevPx and 0xFF)
                }
            }
            // Vertical pass
            for (x in 0 until w) {
                var rSum = 0
                var gSum = 0
                var bSum = 0
                for (dy in -radius..radius) {
                    val px = tmp[dy.coerceIn(0, h - 1) * w + x]
                    rSum += (px ushr 16) and 0xFF
                    gSum += (px ushr 8) and 0xFF
                    bSum += px and 0xFF
                }
                for (y in 0 until h) {
                    pixels[y * w + x] = (0xFF shl 24) or ((rSum / div) shl 16) or ((gSum / div) shl 8) or (bSum / div)
                    val nextY = (y + radius + 1).coerceAtMost(h - 1)
                    val prevY = (y - radius).coerceAtLeast(0)
                    val nextPx = tmp[nextY * w + x]
                    val prevPx = tmp[prevY * w + x]
                    rSum += ((nextPx ushr 16) and 0xFF) - ((prevPx ushr 16) and 0xFF)
                    gSum += ((nextPx ushr 8) and 0xFF) - ((prevPx ushr 8) and 0xFF)
                    bSum += (nextPx and 0xFF) - (prevPx and 0xFF)
                }
            }
        }

        val out = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.RGB_565)
        out.setPixels(pixels, 0, w, 0, 0, w, h)
        scaled.recycle()
        out.asImageBitmap().also { cachedFrostedWisteriaBackdrop = it }
    } catch (_: Throwable) {
        ImageBitmap(1, 1)
    }
}

val LocalAetheriaRootPos = androidx.compose.runtime.compositionLocalOf { mutableStateOf(Offset.Zero) }
val LocalAetheriaRootSize = androidx.compose.runtime.compositionLocalOf { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }

@Composable
fun Modifier.aetheriaBackdropBlur(
    rootSize: androidx.compose.ui.unit.IntSize,
    blurredBitmap: ImageBitmap,
    alpha: Float = 0.65f
): Modifier {
    val rootPosState = LocalAetheriaRootPos.current
    val rootSizeState = LocalAetheriaRootSize.current
    val coordsState = remember { mutableStateOf(Offset.Zero) }
    return this
        .onGloballyPositioned { layoutCoordinates ->
            if (layoutCoordinates.isAttached) {
                coordsState.value = layoutCoordinates.positionInRoot()
            }
        }
        .drawBehind {
            val imgW = blurredBitmap.width
            val imgH = blurredBitmap.height
            if (imgW > 1 && imgH > 1) {
                val coords = coordsState.value
                val rootPos = rootPosState.value
                val currentRootSize = rootSizeState.value
                val relX = coords.x - rootPos.x
                val relY = coords.y - rootPos.y
                val rW = when {
                    currentRootSize.width > 0 -> currentRootSize.width.toFloat()
                    rootSize.width > 0 -> rootSize.width.toFloat()
                    else -> size.width
                }
                val rH = when {
                    currentRootSize.height > 0 -> currentRootSize.height.toFloat()
                    rootSize.height > 0 -> rootSize.height.toFloat()
                    else -> size.height
                }
                val scale = maxOf(rW / imgW.toFloat(), rH / imgH.toFloat())
                val scaledW = imgW * scale
                val scaledH = imgH * scale
                val rootLeft = (rW - scaledW) / 2f
                val rootTop = (rH - scaledH) / 2f
                drawImage(
                    image = blurredBitmap,
                    dstOffset = androidx.compose.ui.unit.IntOffset(
                        kotlin.math.round(rootLeft - relX).toInt(),
                        kotlin.math.round(rootTop - relY).toInt()
                    ),
                    dstSize = androidx.compose.ui.unit.IntSize(
                        kotlin.math.round(scaledW).toInt().coerceAtLeast(1),
                        kotlin.math.round(scaledH).toInt().coerceAtLeast(1)
                    ),
                    alpha = alpha,
                    filterQuality = FilterQuality.High
                )
            }
        }
}

private fun smoothRamp(value: Float, edge0: Float, edge1: Float): Float {
    if (edge0 == edge1) return if (value >= edge1) 1f else 0f
    val t = ((value - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}

class AetheriaRefGlassBitmaps(
    val headerNoLetter: ImageBitmap,
    val sigilNoLetter: ImageBitmap,
    val userTop: ImageBitmap,
    val userMid: ImageBitmap,
    val userBottom: ImageBitmap,
    val botTop: ImageBitmap,
    val botMid: ImageBitmap,
    val botBottom: ImageBitmap,
    val bot3Top: ImageBitmap,
    val bot3Mid: ImageBitmap,
    val bot3Bottom: ImageBitmap,
    val pillsRowAligned: ImageBitmap,
    val quickCards: ImageBitmap,
    val inputBarNoText: ImageBitmap
)

private fun decodeRawRefBitmap(context: android.content.Context, resId: Int): android.graphics.Bitmap? {
    val opts = android.graphics.BitmapFactory.Options().apply {
        inScaled = false
        inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
    }
    return android.graphics.BitmapFactory.decodeResource(context.resources, resId, opts)
}

// Processes bot 3-slice bitmaps (841px wide) so the broken left sigil crop (x < 121) is cleanly removed
// (we render the 100% symmetrical img_chat_ref_sigil_noletter separately in a 1:1 square Box!), while the
// 4-point star, lavender rim border, outer glow, top-right stardust & bottom reflection stay at 100% opacity
// and the interior glass becomes see-through (interiorAlpha = 0.44f).
private fun processBotSliceBitmap(
    context: android.content.Context,
    resId: Int,
    role: String,
    interiorAlpha: Float = 0.44f
): ImageBitmap {
    return try {
        val src = decodeRawRefBitmap(context, resId) ?: return ImageBitmap(1, 1)
        val w = src.width
        val h = src.height
        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)
        src.recycle()

        val fxTable = FloatArray(w) { x ->
            when {
                x <= 128 -> 1.0f // Left lavender rim border
                x in 129..145 -> 1.0f - smoothRamp(x.toFloat(), 128f, 145f)
                x in 146..756 -> 0.0f // See-through glass interior
                x in 757..776 -> smoothRamp(x.toFloat(), 757f, 776f) // Right smoky-mauve inner glow -> rim
                else -> 1.0f // Right lavender rim border & outer shadow
            }
        }

        for (y in 0 until h) {
            val fy = when (role) {
                "top" -> when {
                    y <= 32 -> 1.0f // Top 4-point star, dark-plum peak & top lavender rim border
                    y in 33..50 -> 1.0f - smoothRamp(y.toFloat(), 32f, 50f)
                    else -> 0.0f
                }
                "bottom" -> when {
                    y <= 34 -> 0.0f
                    y in 35..50 -> smoothRamp(y.toFloat(), 34f, 50f)
                    else -> 1.0f // Bottom lavender rim border, bottom-left reflection & outer shadow
                }
                else -> 0.0f // "mid"
            }
            val rowOffset = y * w
            for (x in 0 until w) {
                // Erase the mis-cropped left sigil region (x < 120) so we can render the pristine 1:1 sigil medallion cleanly
                if (x < 120) {
                    pixels[rowOffset + x] = 0
                    continue
                }
                val px = pixels[rowOffset + x]
                val a = (px ushr 24) and 0xFF
                if (a > 0) {
                    var edge = maxOf(fxTable[x], fy)
                    // Preserve top-right cosmic stardust & smoky mauve corner mist in the "top" slice
                    if (role == "top" && x in 665..780 && y in 28..82) {
                        val cxFactor = smoothRamp(x.toFloat(), 665f, 755f)
                        val cyFactor = 1.0f - smoothRamp(y.toFloat(), 32f, 82f)
                        edge = maxOf(edge, cxFactor * cyFactor * 0.78f)
                    }
                    val mult = interiorAlpha + (1.0f - interiorAlpha) * edge
                    // Smooth feather on x = 120..123 so the outer left shadow blends seamlessly
                    val leftFeather = if (x in 120..122) (x - 119) / 3f else 1f
                    val na = (a * mult * leftFeather).toInt().coerceIn(0, 255)
                    pixels[rowOffset + x] = (na shl 24) or (px and 0x00FFFFFF)
                }
            }
        }
        val out = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888)
        out.setPixels(pixels, 0, w, 0, 0, w, h)
        out.asImageBitmap()
    } catch (_: Throwable) {
        ImageBitmap(1, 1)
    }
}

// Processes user 3-slice bitmaps (841px wide, bubble at x=169..815) so the amethyst rim border,
// top-center specular sheen & bottom-left reflection stay at 100% opacity while the interior glass is see-through.
private fun processUserSliceBitmap(
    context: android.content.Context,
    resId: Int,
    role: String,
    interiorAlpha: Float = 0.52f
): ImageBitmap {
    return try {
        val src = decodeRawRefBitmap(context, resId) ?: return ImageBitmap(1, 1)
        val w = src.width
        val h = src.height
        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)
        src.recycle()

        val fxTable = FloatArray(w) { x ->
            when {
                x <= 174 -> 1.0f
                x in 175..190 -> 1.0f - smoothRamp(x.toFloat(), 174f, 190f)
                x in 191..795 -> 0.0f
                x in 796..810 -> smoothRamp(x.toFloat(), 796f, 810f)
                else -> 1.0f
            }
        }

        for (y in 0 until h) {
            val fy = when (role) {
                "top" -> when {
                    y <= 14 -> 1.0f
                    y in 15..28 -> 1.0f - smoothRamp(y.toFloat(), 14f, 28f)
                    else -> 0.0f
                }
                "bottom" -> when {
                    y <= 22 -> 0.0f
                    y in 23..38 -> smoothRamp(y.toFloat(), 22f, 38f)
                    else -> 1.0f
                }
                else -> 0.0f
            }
            val rowOffset = y * w
            for (x in 0 until w) {
                val px = pixels[rowOffset + x]
                val a = (px ushr 24) and 0xFF
                if (a > 0) {
                    val edge = maxOf(fxTable[x], fy)
                    val mult = interiorAlpha + (1.0f - interiorAlpha) * edge
                    val na = (a * mult).toInt().coerceIn(0, 255)
                    pixels[rowOffset + x] = (na shl 24) or (px and 0x00FFFFFF)
                }
            }
        }
        val out = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888)
        out.setPixels(pixels, 0, w, 0, 0, w, h)
        out.asImageBitmap()
    } catch (_: Throwable) {
        ImageBitmap(1, 1)
    }
}

// Shifts img_chat_ref_pills_row.png (841x98) right by +81px so [Kopyala] [Devam Et] [Diğer] start
// at the exact left edge of the bot message box (x=123/841) and end at the exact right edge (x=782/841),
// while replacing the flat vertical crop cuts at the far-left of Kopyala and far-right of Diğer with
// a smooth 360° radial amethyst-lavender glow transition!
private fun processAlignedPillsRowBitmap(
    context: android.content.Context,
    resId: Int,
    shiftPx: Int = 81,
    interiorAlpha: Float = 0.62f
): ImageBitmap {
    return try {
        val src = decodeRawRefBitmap(context, resId) ?: return ImageBitmap(1, 1)
        val w = src.width
        val h = src.height
        val srcPixels = IntArray(w * h)
        src.getPixels(srcPixels, 0, w, 0, 0, w, h)
        src.recycle()

        // Left semicircle center of Kopyala after +81px shift: cxLeft = 159f, cy = 49f (pill radius = 36.5f)
        // Right semicircle center of Diğer after +81px shift: cxRight = 746f, cy = 49f (pill radius = 36.5f)
        val cxLeft = 78f + shiftPx
        val cxRight = 665f + shiftPx
        val cyCap = 49f
        val pillR = 36.5f
        val outerGlowMaxR = 56f

        val dstPixels = IntArray(w * h)
        for (y in 0 until h) {
            val rowOffset = y * w
            val fyEdge = when {
                y <= 21 -> 1.0f
                y in 22..30 -> 1.0f - smoothRamp(y.toFloat(), 21f, 30f)
                y in 31..67 -> 0.0f
                y in 68..76 -> smoothRamp(y.toFloat(), 68f, 76f)
                else -> 1.0f
            }
            for (x in 0 until w) {
                val srcX = x - shiftPx
                var pixelWritten = false

                // Check if we are in the outer left cap zone (x < cxLeft) or outer right cap zone (x > cxRight)
                val isLeftCapZone = x < cxLeft
                val isRightCapZone = x > cxRight
                val capDist = when {
                    isLeftCapZone -> kotlin.math.hypot((x - cxLeft).toDouble(), (y - cyCap).toDouble()).toFloat()
                    isRightCapZone -> kotlin.math.hypot((x - cxRight).toDouble(), (y - cyCap).toDouble()).toFloat()
                    else -> 0f
                }

                // For the outer glow outside the left/right semicircle border (capDist > pillR),
                // blend a smooth radial amethyst-violet aura so there is NEVER a flat vertical cutoff!
                if ((isLeftCapZone || isRightCapZone) && capDist > pillR) {
                    if (capDist < outerGlowMaxR) {
                        val radialFade = 1.0f - smoothRamp(capDist, pillR - 0.5f, outerGlowMaxR)
                        val glowAlpha = (radialFade * radialFade * 155f).toInt().coerceIn(0, 255)
                        if (glowAlpha > 0) {
                            // Soft amethyst-lilac glow (#B87CFA)
                            dstPixels[rowOffset + x] = (glowAlpha shl 24) or (184 shl 16) or (124 shl 8) or 250
                        }
                    }
                    pixelWritten = true
                }

                if (!pixelWritten && srcX in 0 until w) {
                    val px = srcPixels[rowOffset + srcX]
                    val a = (px ushr 24) and 0xFF
                    if (a > 0) {
                        val r = (px ushr 16) and 0xFF
                        val g = (px ushr 8) and 0xFF
                        val b = px and 0xFF
                        val lum = maxOf(r, g, b)
                        val lumFactor = smoothRamp(lum.toFloat(), 88f, 138f)
                        val edge = maxOf(fyEdge, lumFactor)
                        val mult = interiorAlpha + (1.0f - interiorAlpha) * edge
                        val na = (a * mult).toInt().coerceIn(0, 255)
                        dstPixels[rowOffset + x] = (na shl 24) or (px and 0x00FFFFFF)
                    }
                }
            }
        }
        val out = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888)
        out.setPixels(dstPixels, 0, w, 0, 0, w, h)
        out.asImageBitmap()
    } catch (_: Throwable) {
        ImageBitmap(1, 1)
    }
}

// Processes header, quick-cards, and input-bar plates so all glowing borders, icons, text, and feathered edges
// remain at 100% while the dark glass interiors become translucent over the frosted wisteria background.
private fun processPlateWithTranslucentInterior(
    context: android.content.Context,
    resId: Int,
    interiorAlpha: Float = 0.64f
): ImageBitmap {
    return try {
        val src = decodeRawRefBitmap(context, resId) ?: return ImageBitmap(1, 1)
        val w = src.width
        val h = src.height
        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)
        src.recycle()

        for (i in pixels.indices) {
            val px = pixels[i]
            val a = (px ushr 24) and 0xFF
            if (a > 0) {
                val r = (px ushr 16) and 0xFF
                val g = (px ushr 8) and 0xFF
                val b = px and 0xFF
                val lum = maxOf(r, g, b)
                // Bright pixels (borders, icons, text, neon send orb) stay at 1.0f; dark interior glass scales by interiorAlpha
                val lumPreserve = smoothRamp(lum.toFloat(), 82f, 135f)
                val mult = interiorAlpha + (1.0f - interiorAlpha) * lumPreserve
                val na = (a * mult).toInt().coerceIn(0, 255)
                pixels[i] = (na shl 24) or (px and 0x00FFFFFF)
            }
        }
        val out = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888)
        out.setPixels(pixels, 0, w, 0, 0, w, h)
        out.asImageBitmap()
    } catch (_: Throwable) {
        ImageBitmap(1, 1)
    }
}

fun loadAetheriaRefGlassBitmaps(context: android.content.Context): AetheriaRefGlassBitmaps {
    val rawSigil = decodeRawRefBitmap(context, com.example.R.drawable.img_chat_ref_sigil_noletter)
    val sigilBitmap = rawSigil?.asImageBitmap() ?: ImageBitmap(1, 1)
    return AetheriaRefGlassBitmaps(
        headerNoLetter = processPlateWithTranslucentInterior(context, com.example.R.drawable.img_chat_ref_header_noletter, interiorAlpha = 0.78f),
        sigilNoLetter = sigilBitmap,
        userTop = processUserSliceBitmap(context, com.example.R.drawable.img_chat_ref_user_top, role = "top", interiorAlpha = 0.52f),
        userMid = processUserSliceBitmap(context, com.example.R.drawable.img_chat_ref_user_mid, role = "mid", interiorAlpha = 0.52f),
        userBottom = processUserSliceBitmap(context, com.example.R.drawable.img_chat_ref_user_bottom, role = "bottom", interiorAlpha = 0.52f),
        botTop = processBotSliceBitmap(context, com.example.R.drawable.img_chat_ref_bot_top_noletter, role = "top", interiorAlpha = 0.44f),
        botMid = processBotSliceBitmap(context, com.example.R.drawable.img_chat_ref_bot_mid, role = "mid", interiorAlpha = 0.44f),
        botBottom = processBotSliceBitmap(context, com.example.R.drawable.img_chat_ref_bot_bottom, role = "bottom", interiorAlpha = 0.44f),
        bot3Top = processBotSliceBitmap(context, com.example.R.drawable.img_chat_ref_bot3_top_noletter, role = "top", interiorAlpha = 0.44f),
        bot3Mid = processBotSliceBitmap(context, com.example.R.drawable.img_chat_ref_bot3_mid, role = "mid", interiorAlpha = 0.44f),
        bot3Bottom = processBotSliceBitmap(context, com.example.R.drawable.img_chat_ref_bot3_bottom, role = "bottom", interiorAlpha = 0.44f),
        pillsRowAligned = processAlignedPillsRowBitmap(context, com.example.R.drawable.img_chat_ref_pills_row, shiftPx = 81, interiorAlpha = 0.62f),
        quickCards = processPlateWithTranslucentInterior(context, com.example.R.drawable.img_chat_ref_quick_cards, interiorAlpha = 0.60f),
        inputBarNoText = processPlateWithTranslucentInterior(context, com.example.R.drawable.img_chat_ref_input_bar_notext, interiorAlpha = 0.58f)
    )
}

// Draws a 3-slice reference glass bubble (top, mid, bottom) over a continuous see-through frosted wisteria backdrop
// clipped strictly inside the bubble's rounded rectangle so the background shines through while the 1:1 reference
// glow ("Flow"), rim border, top 4-point star (✦), and corner stardust remain 100% crisp.
@Composable
fun Modifier.aetheriaThreeSliceGlassBubble(
    rootSize: androidx.compose.ui.unit.IntSize,
    blurredBitmap: ImageBitmap,
    topSlice: ImageBitmap,
    midSlice: ImageBitmap,
    bottomSlice: ImageBitmap,
    isUserBubble: Boolean,
    frostedAlpha: Float = 0.62f
): Modifier {
    val rootPosState = LocalAetheriaRootPos.current
    val rootSizeState = LocalAetheriaRootSize.current
    val coordsState = remember { mutableStateOf(Offset.Zero) }
    return this
        .onGloballyPositioned { layoutCoordinates ->
            if (layoutCoordinates.isAttached) {
                coordsState.value = layoutCoordinates.positionInRoot()
            }
        }
        .drawBehind {
            val w = size.width
            val h = size.height
            if (w > 4f && h > 4f && topSlice.width > 1 && midSlice.width > 1 && bottomSlice.width > 1) {
                val scale = w / topSlice.width.toFloat()
                val rawTopH = topSlice.height * scale
                val rawBotH = bottomSlice.height * scale
                val totalCaps = rawTopH + rawBotH
                val capScale = if (h < totalCaps && totalCaps > 1f) h / totalCaps else 1f
                val topIntH = kotlin.math.round(rawTopH * capScale).toInt().coerceAtLeast(1)
                val botIntH = kotlin.math.round(rawBotH * capScale).toInt().coerceAtLeast(1)
                val totalIntH = kotlin.math.round(h).toInt().coerceAtLeast(topIntH + botIntH)
                val midIntH = (totalIntH - topIntH - botIntH).coerceAtLeast(0)
                val intW = kotlin.math.round(w).toInt().coerceAtLeast(1)

                // 1. Continuous frosted wisteria background clipped strictly inside the bubble's rim border
                val imgW = blurredBitmap.width
                val imgH = blurredBitmap.height
                if (imgW > 1 && imgH > 1) {
                    val leftIn = if (isUserBubble) w * (171f / 841f) else w * (125f / 841f)
                    val rightIn = if (isUserBubble) w * (814f / 841f) else w * (780f / 841f)
                    val topIn = if (isUserBubble) topIntH * (11f / 52f) else topIntH * (30f / 116f)
                    val botIn = if (isUserBubble) {
                        totalIntH - botIntH * ((52f - 41f) / 52f)
                    } else {
                        totalIntH - botIntH * ((64f - 54f) / 64f)
                    }
                    val cr = w * (36f / 841f)
                    val clipPath = Path().apply {
                        addRoundRect(
                            androidx.compose.ui.geometry.RoundRect(
                                left = leftIn,
                                top = topIn,
                                right = rightIn,
                                bottom = botIn.coerceAtLeast(topIn + 1f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cr, cr)
                            )
                        )
                    }
                    drawContext.canvas.save()
                    drawContext.canvas.clipPath(clipPath)
                    val coords = coordsState.value
                    val rootPos = rootPosState.value
                    val currentRootSize = rootSizeState.value
                    val relX = coords.x - rootPos.x
                    val relY = coords.y - rootPos.y
                    val rW = when {
                        currentRootSize.width > 0 -> currentRootSize.width.toFloat()
                        rootSize.width > 0 -> rootSize.width.toFloat()
                        else -> w
                    }
                    val rH = when {
                        currentRootSize.height > 0 -> currentRootSize.height.toFloat()
                        rootSize.height > 0 -> rootSize.height.toFloat()
                        else -> h
                    }
                    val bgScale = maxOf(rW / imgW.toFloat(), rH / imgH.toFloat())
                    val scaledW = imgW * bgScale
                    val scaledH = imgH * bgScale
                    val rootLeft = (rW - scaledW) / 2f
                    val rootTop = (rH - scaledH) / 2f
                    drawImage(
                        image = blurredBitmap,
                        dstOffset = androidx.compose.ui.unit.IntOffset(
                            kotlin.math.round(rootLeft - relX).toInt(),
                            kotlin.math.round(rootTop - relY).toInt()
                        ),
                        dstSize = androidx.compose.ui.unit.IntSize(
                            kotlin.math.round(scaledW).toInt().coerceAtLeast(1),
                            kotlin.math.round(scaledH).toInt().coerceAtLeast(1)
                        ),
                        alpha = frostedAlpha,
                        filterQuality = FilterQuality.High
                    )
                    drawContext.canvas.restore()
                }

                // 2. Top slice (1:1 reference border, glow, 4-point star ✦, 'A' sigil ring, and top-right stardust)
                drawImage(
                    image = topSlice,
                    dstOffset = androidx.compose.ui.unit.IntOffset(0, 0),
                    dstSize = androidx.compose.ui.unit.IntSize(intW, topIntH),
                    filterQuality = FilterQuality.High
                )

                // 3. Middle slice (seamless translucent glass body + 100% crisp left/right lavender rim borders)
                if (midIntH > 0) {
                    drawImage(
                        image = midSlice,
                        dstOffset = androidx.compose.ui.unit.IntOffset(0, topIntH),
                        dstSize = androidx.compose.ui.unit.IntSize(intW, midIntH),
                        filterQuality = FilterQuality.High
                    )
                }

                // 4. Bottom slice (1:1 reference bottom lavender rim border, bottom-left purple reflection & outer shadow)
                drawImage(
                    image = bottomSlice,
                    dstOffset = androidx.compose.ui.unit.IntOffset(0, topIntH + midIntH),
                    dstSize = androidx.compose.ui.unit.IntSize(intW, botIntH),
                    filterQuality = FilterQuality.High
                )
            }
        }
}

// Authentic 1:1 reference purple-smoked glass surface (matches img_chat_ref_bot_bubble_plate.png):
// ZERO white wash! Uses deep smoked night-plum in the center, purple window refraction on the left,
// and smoky mauve-orchid inner glow + subtle cosmic stardust specks in the top-right corner.
fun Modifier.morCamGlassSurface(
    baseSmokeColor: Color = Color(0x58160B2B),
    topRightFrostAlpha: Float = 0.28f,
    topLeftFrostAlpha: Float = 0.20f,
    topStarFrostAlpha: Float = 0.22f,
    bottomEdgeRefractionAlpha: Float = 0.18f,
    cornerRadius: androidx.compose.ui.unit.Dp = 22.dp
): Modifier = this.drawBehind {
    val w = size.width
    val h = size.height
    // 1. Deep smoked night-plum glass body (#211536 center while letting left purple stained-glass window shine through)
    drawRect(color = baseSmokeColor)

    // 2. Subtle purple-mauve inner perimeter refraction (clipped inside by .clip(...) — NEVER white!)
    if (w > 2f && h > 2f) {
        val cr = cornerRadius.toPx()
        drawIntoCanvas { canvas ->
            val innerPurplePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 3.0.dp.toPx()
                color = android.graphics.Color.argb(52, 148, 98, 208)
                maskFilter = android.graphics.BlurMaskFilter(5.5.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
            }
            canvas.nativeCanvas.drawRoundRect(0f, 0f, w, h, cr, cr, innerPurplePaint)
        }
    }

    // 3. Right & top-right smoky mauve-orchid inner glow + subtle cosmic stardust specks (from img_chat_ref_bot_bubble_plate.png)
    if (topRightFrostAlpha > 0f && w > 1f && h > 1f) {
        // Right inner edge smoky mauve glow (x = 0.82..1.0 -> #6f4b7b..#7b5686 in reference pixel scan)
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFF6F4888).copy(alpha = topRightFrostAlpha * 0.45f),
                    Color(0xFF8C5CA2).copy(alpha = topRightFrostAlpha * 0.88f)
                ),
                startX = w * 0.78f,
                endX = w
            )
        )
        val trCenter = Offset(w * 0.88f, h * 0.12f)
        val trRadius = maxOf(w * 0.34f, h * 0.44f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF9E6BB8).copy(alpha = topRightFrostAlpha * 0.85f),
                    Color(0xFF754794).copy(alpha = topRightFrostAlpha * 0.40f),
                    Color.Transparent
                ),
                center = trCenter,
                radius = trRadius
            ),
            radius = trRadius,
            center = trCenter
        )
        // Tiny cosmic stardust specks in the top-right corner (visible in img_chat_ref_bot_bubble_plate.png)
        if (w > 160.dp.toPx() && h > 70.dp.toPx()) {
            val specks = listOf(
                Offset(0.89f, 0.06f) to 0.9f,
                Offset(0.93f, 0.09f) to 1.1f,
                Offset(0.95f, 0.14f) to 0.8f,
                Offset(0.91f, 0.13f) to 0.7f,
                Offset(0.96f, 0.20f) to 0.85f,
                Offset(0.86f, 0.08f) to 0.75f
            )
            specks.forEach { (pt, radDp) ->
                drawCircle(
                    color = Color(0xFFD8B4FE).copy(alpha = 0.42f),
                    radius = radDp.dp.toPx(),
                    center = Offset(w * pt.x, h * pt.y)
                )
            }
        }
    }

    // 4. Top-left purple stained-glass window refraction (amethyst-violet #7E4AC2, NEVER white)
    if (topLeftFrostAlpha > 0f && w > 1f && h > 1f) {
        val tlCenter = Offset(w * 0.12f, h * 0.12f)
        val tlRadius = maxOf(w * 0.34f, h * 0.42f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF9362D6).copy(alpha = topLeftFrostAlpha * 0.90f),
                    Color(0xFF6B3FA8).copy(alpha = topLeftFrostAlpha * 0.42f),
                    Color.Transparent
                ),
                center = tlCenter,
                radius = tlRadius
            ),
            radius = tlRadius,
            center = tlCenter
        )
    }

    // 5. Top-center starlight violet refraction right under the 4-point star
    if (topStarFrostAlpha > 0f && w > 1f && h > 1f) {
        val tcCenter = Offset(w * 0.50f, 0f)
        val tcRadius = w * 0.20f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFC89BFF).copy(alpha = topStarFrostAlpha * 0.85f),
                    Color(0xFF8B5CF6).copy(alpha = topStarFrostAlpha * 0.35f),
                    Color.Transparent
                ),
                center = tcCenter,
                radius = tcRadius
            ),
            radius = tcRadius,
            center = tcCenter
        )
    }

    // 6. Subtle bottom-left purple window reflection (matching #caabe9 / #965acc at x=0.18..0.34 in reference scan)
    if (bottomEdgeRefractionAlpha > 0f && h > 1f) {
        val blCenter = Offset(w * 0.24f, h)
        val blRadius = maxOf(w * 0.28f, h * 0.28f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF9B6AD6).copy(alpha = bottomEdgeRefractionAlpha * 0.75f),
                    Color(0xFF6D3FA6).copy(alpha = bottomEdgeRefractionAlpha * 0.30f),
                    Color.Transparent
                ),
                center = blCenter,
                radius = blRadius
            ),
            radius = blRadius,
            center = blCenter
        )
    }
}

// Stage 1 (BEFORE .clip): Soft blurred dark-plum outer cushion + violet-purple rim aura (matches img_chat_ref_bot_bubble_plate.png)
fun Modifier.morCamOuterGlow(
    cornerRadius: androidx.compose.ui.unit.Dp = 24.dp,
    shadowAlpha: Float = 0.42f,
    rimGlowAlpha: Float = 0.26f,
    bottomFlareAlpha: Float = 0.45f,
    flareColor: Color = Color(0xFF9D6CE0)
): Modifier = this.drawBehind {
    val cr = cornerRadius.toPx()
    drawIntoCanvas { canvas ->
        val native = canvas.nativeCanvas
        // 1. Soft dark-plum outer blurred cushion surrounding the entire shape (visible in all img_chat_ref_* plates)
        if (shadowAlpha > 0f) {
            val cushionPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 5.5.dp.toPx()
                color = android.graphics.Color.argb((shadowAlpha * 235).toInt().coerceIn(0, 255), 22, 9, 42)
                maskFilter = android.graphics.BlurMaskFilter(5.5.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
            }
            native.drawRoundRect(0f, 0f, size.width, size.height, cr, cr, cushionPaint)
        }
        // 2. Subtle purple-violet outer glow (NEVER white)
        if (rimGlowAlpha > 0f) {
            val rimPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 2.0.dp.toPx()
                color = android.graphics.Color.argb((rimGlowAlpha * 220).toInt().coerceIn(0, 255), 147, 88, 232)
                maskFilter = android.graphics.BlurMaskFilter(4.5.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
            }
            native.drawRoundRect(0f, 0f, size.width, size.height, cr, cr, rimPaint)
        }
        // 3. Localized purple-orchid bottom edge glow
        if (bottomFlareAlpha > 0f) {
            val flarePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 2.2.dp.toPx()
                color = android.graphics.Color.argb(
                    (bottomFlareAlpha * 145).toInt().coerceIn(0, 255),
                    158,
                    104,
                    230
                )
                maskFilter = android.graphics.BlurMaskFilter(4.0.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
            }
            native.drawLine(
                size.width * 0.16f,
                size.height,
                size.width * 0.84f,
                size.height,
                flarePaint
            )
        }
    }
}

// Stage 2 (AFTER .background & .border): Subtle purple-lilac rim glints (NO white horizontal bars!)
fun Modifier.morCamTopAndBottomSpecular(
    cornerRadius: androidx.compose.ui.unit.Dp = 24.dp,
    topSheenAlpha: Float = 0.24f,
    bottomFlareAlpha: Float = 0.78f,
    flareColor: Color = Color(0xFFB985F5)
): Modifier = this.drawWithContent {
    drawContent()
    val inset = cornerRadius.toPx().coerceAtMost(size.width * 0.25f)
    // Subtle top-left purple-lilac glint (matching x=0.18..0.38 on top border in img_chat_ref_bot_bubble_plate.png)
    if (topSheenAlpha > 0f && size.width > inset * 2f) {
        val sx = inset * 0.75f
        val ex = size.width * 0.46f
        if (ex > sx) {
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        Color(0xFFB380F5).copy(alpha = topSheenAlpha * 0.75f),
                        Color(0xFFD8B4FE).copy(alpha = topSheenAlpha * 0.95f),
                        Color(0xFFA86EE8).copy(alpha = topSheenAlpha * 0.65f),
                        Color.Transparent
                    ),
                    startX = sx,
                    endX = ex
                ),
                start = Offset(sx, 0.65.dp.toPx()),
                end = Offset(ex, 0.65.dp.toPx()),
                strokeWidth = 0.9.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
    // Subtle bottom-left / bottom-center violet-lilac rim reflection (purple-lilac #CAABE9 / #9B6EE3, NEVER white)
    if (bottomFlareAlpha > 0f && size.width > inset * 2f) {
        val sx = inset * 0.70f
        val ex = size.width * 0.58f
        val y = size.height - 0.65.dp.toPx()
        if (ex > sx) {
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        flareColor.copy(alpha = bottomFlareAlpha * 0.35f),
                        Color(0xFFD6B4FC).copy(alpha = bottomFlareAlpha * 0.55f),
                        flareColor.copy(alpha = bottomFlareAlpha * 0.30f),
                        Color.Transparent
                    ),
                    startX = sx,
                    endX = ex
                ),
                start = Offset(sx, y),
                end = Offset(ex, y),
                strokeWidth = 1.0.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

// 1:1 Reference Cosmic Nebula 'A' Avatar Medallion with Radiant Lens Flares, Inner Mini Diamonds & Metallic Serif Letter
@Composable
fun AidenMagicSigilAvatar(
    totalSize: androidx.compose.ui.unit.Dp,
    contentDescription: String? = null,
    initial: String = "A",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(totalSize),
        contentAlignment = Alignment.Center
    ) {
        val coreSize = totalSize * 0.84f
        val ringRFraction = 0.415f

        // Crisp circular cosmic nebula core
        Box(
            modifier = Modifier
                .size(coreSize)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF531E8C),
                            Color(0xFF280D4D),
                            Color(0xFF100524)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Nebula wisps, stardust & 3/9 o'clock 4-point mini diamonds matching reference medallion
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xBBE879F9), Color(0x55A855F7), Color.Transparent),
                        center = Offset(w * 0.76f, h * 0.78f),
                        radius = w * 0.42f
                    ),
                    radius = w * 0.42f,
                    center = Offset(w * 0.76f, h * 0.78f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xAA9333EA), Color(0x447C3AED), Color.Transparent),
                        center = Offset(w * 0.24f, h * 0.22f),
                        radius = w * 0.40f
                    ),
                    radius = w * 0.40f,
                    center = Offset(w * 0.24f, h * 0.22f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x66C084FC), Color.Transparent),
                        center = Offset(w * 0.78f, h * 0.26f),
                        radius = w * 0.30f
                    ),
                    radius = w * 0.30f,
                    center = Offset(w * 0.78f, h * 0.26f)
                )
                // Mini 4-point stars at 9 o'clock and 3 o'clock inside the ring
                val miniStarCenters = listOf(
                    Offset(w * 0.16f, h * 0.50f),
                    Offset(w * 0.84f, h * 0.50f)
                )
                val starR = w * 0.068f
                val starW = w * 0.018f
                miniStarCenters.forEach { c ->
                    val miniPath = Path().apply {
                        moveTo(c.x, c.y - starR)
                        lineTo(c.x + starW, c.y - starW)
                        lineTo(c.x + starR, c.y)
                        lineTo(c.x + starW, c.y + starW)
                        lineTo(c.x, c.y + starR)
                        lineTo(c.x - starW, c.y + starW)
                        lineTo(c.x - starR, c.y)
                        lineTo(c.x - starW, c.y - starW)
                        close()
                    }
                    drawPath(miniPath, Color(0xFFE9D5FF))
                }
                val specks = listOf(
                    Offset(0.24f, 0.30f) to 1.1f,
                    Offset(0.76f, 0.25f) to 1.0f,
                    Offset(0.22f, 0.70f) to 0.9f,
                    Offset(0.75f, 0.75f) to 1.4f,
                    Offset(0.50f, 0.17f) to 0.9f,
                    Offset(0.50f, 0.84f) to 1.0f
                )
                specks.forEach { (pt, rad) ->
                    drawCircle(
                        color = Color(0xFFF3E8FF).copy(alpha = 0.88f),
                        radius = rad.dp.toPx(),
                        center = Offset(w * pt.x, h * pt.y)
                    )
                }
            }

            // Crisp metallic silver-lilac serif initial matching reference medallion
            val aFontSize = (totalSize.value * 0.45f).sp
            Text(
                text = initial.ifBlank { "A" }.take(1).uppercase(),
                fontSize = aFontSize,
                fontFamily = DisplaySerifFontFamily,
                fontWeight = FontWeight.Normal,
                style = TextStyle(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFF3E8FF),
                            Color(0xFFD8B4FE)
                        )
                    ),
                    shadow = Shadow(
                        color = Color(0xCCA855F7),
                        offset = Offset(0f, 0f),
                        blurRadius = 10f
                    )
                )
            )
        }

        // Outer neon purple-pink aura + crisp inner circle + diagonal lens-flare glints + delicate outer celestial arc + 4/8 needle points
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val ringR = size.minDimension * ringRFraction
            val outerTipCard = size.minDimension * 0.495f
            val outerTipDiag = size.minDimension * 0.456f

            drawIntoCanvas { canvas ->
                val native = canvas.nativeCanvas
                val auraPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = 3.0.dp.toPx()
                    color = android.graphics.Color.argb(185, 192, 132, 252)
                    maskFilter = android.graphics.BlurMaskFilter(5.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
                }
                native.drawCircle(cx, cy, ringR, auraPaint)
            }

            // Faint outer celestial arc circle
            drawCircle(
                color = Color(0x709333EA),
                radius = ringR * 1.09f,
                center = Offset(cx, cy),
                style = Stroke(width = 0.7.dp.toPx())
            )

            // Crisp luminous inner circle border
            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(
                        Color(0xFFF3E8FF),
                        Color(0xFFC084FC),
                        Color(0xFFFFFFFF),
                        Color(0xFFA855F7),
                        Color(0xFFF3E8FF)
                    )
                ),
                radius = ringR,
                center = Offset(cx, cy),
                style = Stroke(width = 1.25.dp.toPx())
            )

            // Radiant lens-flare bursts on the ring (at ~1:30, ~5:00, ~10:30) just like img_chat_aiden_sigil_avatar
            val flareAngles = listOf(-42.0, 52.0, -135.0)
            flareAngles.forEachIndexed { idx, deg ->
                val rad = Math.toRadians(deg)
                val fx = cx + (Math.cos(rad) * ringR).toFloat()
                val fy = cy + (Math.sin(rad) * ringR).toFloat()
                val fR = if (idx < 2) size.minDimension * 0.14f else size.minDimension * 0.10f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xEEFFFFFF), Color(0xAAE879F9), Color(0x44A855F7), Color.Transparent),
                        center = Offset(fx, fy),
                        radius = fR
                    ),
                    radius = fR,
                    center = Offset(fx, fy)
                )
            }

            // 8 sleek, delicate geometric compass/star needle points protruding slightly outside the circle
            for (i in 0 until 8) {
                val angleDeg = i * 45f
                val isCardinal = i % 2 == 0
                val tipR = if (isCardinal) outerTipCard else outerTipDiag
                val baseHalfW = if (isCardinal) size.minDimension * 0.028f else size.minDimension * 0.018f
                rotate(degrees = angleDeg, pivot = Offset(cx, cy)) {
                    val spikePath = Path().apply {
                        moveTo(cx, cy - tipR)
                        lineTo(cx + baseHalfW, cy - ringR * 0.96f)
                        lineTo(cx, cy - ringR * 0.89f)
                        lineTo(cx - baseHalfW, cy - ringR * 0.96f)
                        close()
                    }
                    drawPath(
                        path = spikePath,
                        brush = Brush.verticalGradient(
                            colors = if (isCardinal) {
                                listOf(Color.White, Color(0xFFE9D5FF), Color(0xFFA855F7))
                            } else {
                                listOf(Color(0xFFE9D5FF), Color(0xFFC084FC), Color(0xFF7C3AED))
                            },
                            startY = cy - tipR,
                            endY = cy - ringR * 0.89f
                        )
                    )
                }
            }
        }
    }
}

// 1:1 Reference Purple Verified Shield/Starburst Badge (mor yıldızlı/kalkan onay rozeti içinde beyaz tik ✓)
@Composable
fun PurpleVerifiedBadge(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(16.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outerR = size.minDimension * 0.46f
        val innerR = size.minDimension * 0.36f

        drawIntoCanvas { canvas ->
            val glowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.argb(175, 168, 85, 247)
                maskFilter = android.graphics.BlurMaskFilter(3.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
            }
            canvas.nativeCanvas.drawCircle(cx, cy, outerR, glowPaint)
        }

        // 8-scallop starburst/shield polygon
        val badgePath = Path().apply {
            val points = 16
            for (i in 0 until points) {
                val angle = Math.toRadians((i * 360.0 / points) - 90.0)
                val r = if (i % 2 == 0) outerR else innerR
                val px = cx + (Math.cos(angle) * r).toFloat()
                val py = cy + (Math.sin(angle) * r).toFloat()
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }
        drawPath(
            path = badgePath,
            brush = Brush.linearGradient(
                listOf(Color(0xFFC084FC), Color(0xFF9333EA), Color(0xFF6D28D9))
            )
        )
        drawPath(
            path = badgePath,
            color = Color(0xCCEDE9FE),
            style = Stroke(width = 0.7.dp.toPx(), join = androidx.compose.ui.graphics.StrokeJoin.Round)
        )

        // Crisp white checkmark (✓) inside
        val checkPath = Path().apply {
            moveTo(cx - outerR * 0.36f, cy + outerR * 0.02f)
            lineTo(cx - outerR * 0.08f, cy + outerR * 0.30f)
            lineTo(cx + outerR * 0.40f, cy - outerR * 0.24f)
        }
        drawPath(
            path = checkPath,
            color = Color.White,
            style = Stroke(
                width = 1.55.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}

// 1:1 Reference 4-point star (✦) with dark-plum curved peak & purple-lilac top ray (matches img_chat_ref_bot_bubble_plate.png & img_chat_ref_top_star.png)
@Composable
fun TopBorderGlowingStar(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier.size(width = 104.dp, height = 28.dp)
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        // 1. Curved dark-plum peak (/\ ) behind the top point of the 4-point star (signature detail in img_chat_ref_bot_bubble_plate.png)
        drawIntoCanvas { canvas ->
            val peakPath = android.graphics.Path().apply {
                moveTo(cx - 22.dp.toPx(), cy)
                quadTo(cx - 5.dp.toPx(), cy, cx, cy - 13.5.dp.toPx())
                quadTo(cx + 5.dp.toPx(), cy, cx + 22.dp.toPx(), cy)
                close()
            }
            val peakPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                style = android.graphics.Paint.Style.FILL
                color = android.graphics.Color.argb(215, 23, 9, 44)
                maskFilter = android.graphics.BlurMaskFilter(3.2.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
            }
            canvas.nativeCanvas.drawPath(peakPath, peakPaint)
        }

        // 2. Thin purple-lilac horizontal ray along the top border (NOT white!)
        drawLine(
            brush = Brush.horizontalGradient(
                listOf(
                    Color.Transparent,
                    Color(0x70935CE0),
                    Color(0xCCBA88F7),
                    Color(0xFFE6CCFF),
                    Color(0xCCBA88F7),
                    Color(0x70935CE0),
                    Color.Transparent
                )
            ),
            start = Offset(0f, cy),
            end = Offset(size.width, cy),
            strokeWidth = 1.15.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        // 3. Soft violet-purple aura behind the 4-point star
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0x88C084FC), Color(0x449333EA), Color.Transparent),
                center = Offset(cx, cy),
                radius = 9.dp.toPx()
            ),
            radius = 9.dp.toPx(),
            center = Offset(cx, cy)
        )

        // 4. Tiny diagonal cross-glints (seen in img_chat_ref_top_star.png)
        val diagLen = 4.2.dp.toPx()
        drawLine(
            color = Color(0x99C084FC),
            start = Offset(cx - diagLen, cy - diagLen),
            end = Offset(cx + diagLen, cy + diagLen),
            strokeWidth = 0.8.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        drawLine(
            color = Color(0x99C084FC),
            start = Offset(cx - diagLen, cy + diagLen),
            end = Offset(cx + diagLen, cy - diagLen),
            strokeWidth = 0.8.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        // 5. Outer amethyst-lilac 4-point star (✦)
        val vOuter = 11.dp.toPx()
        val hOuter = 9.5.dp.toPx()
        val waistOuter = 1.85.dp.toPx()
        val outerStar = Path().apply {
            moveTo(cx, cy - vOuter)
            lineTo(cx + waistOuter, cy - waistOuter)
            lineTo(cx + hOuter, cy)
            lineTo(cx + waistOuter, cy + waistOuter)
            lineTo(cx, cy + vOuter)
            lineTo(cx - waistOuter, cy + waistOuter)
            lineTo(cx - hOuter, cy)
            lineTo(cx - waistOuter, cy - waistOuter)
            close()
        }
        drawPath(path = outerStar, color = Color(0xFFC492FF))

        // 6. Inner pale-lilac star core (✦)
        val vInner = 7.8.dp.toPx()
        val hInner = 6.8.dp.toPx()
        val waistInner = 1.1.dp.toPx()
        val innerStar = Path().apply {
            moveTo(cx, cy - vInner)
            lineTo(cx + waistInner, cy - waistInner)
            lineTo(cx + hInner, cy)
            lineTo(cx + waistInner, cy + waistInner)
            lineTo(cx, cy + vInner)
            lineTo(cx - waistInner, cy + waistInner)
            lineTo(cx - hInner, cy)
            lineTo(cx - waistInner, cy - waistInner)
            close()
        }
        drawPath(path = innerStar, color = Color(0xFFF3E4FF))
    }
}

// Crisp single checkmark icon (✓) for user messages before the bot starts writing a reply
@Composable
fun SingleCheckmarkIcon(
    tint: Color = Color(0xEBFFFFFF),
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(width = 12.dp, height = 11.dp)) {
        val w = size.width
        val h = size.height
        val strokeW = 1.45.dp.toPx()
        val tick = Path().apply {
            moveTo(w * 0.15f, h * 0.56f)
            lineTo(w * 0.42f, h * 0.82f)
            lineTo(w * 0.88f, h * 0.18f)
        }
        drawPath(
            path = tick,
            color = tint,
            style = Stroke(width = strokeW, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
        )
    }
}

// Sleek overlapping double checkmark icon (✓✓) matching reference 22:47 ✓✓, 22:50 ✓✓, 22:53 ✓✓ (crisp white, not purple)
@Composable
fun DoubleCheckmarkIcon(
    tint: Color = Color(0xEBFFFFFF),
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(width = 16.dp, height = 11.dp)) {
        val w = size.width
        val h = size.height
        val strokeW = 1.45.dp.toPx()
        val tick1 = Path().apply {
            moveTo(w * 0.06f, h * 0.56f)
            lineTo(w * 0.28f, h * 0.82f)
            lineTo(w * 0.66f, h * 0.18f)
        }
        val tick2 = Path().apply {
            moveTo(w * 0.36f, h * 0.56f)
            lineTo(w * 0.56f, h * 0.82f)
            lineTo(w * 0.94f, h * 0.18f)
        }
        drawPath(
            path = tick1,
            color = tint,
            style = Stroke(width = strokeW, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
        )
        drawPath(
            path = tick2,
            color = tint,
            style = Stroke(width = strokeW, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
        )
    }
}

// 100% Vector-Sharp Icon Badges for the 3 Action Capsules ([Kopyala] [Devam Et] [Diğer])
@Composable
fun GlassPillIconBadge(type: String, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(26.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension / 2f

        when (type) {
            "copy" -> {
                // Translucent dark circular badge matching img_chat_ref_pills_row
                drawCircle(
                    color = Color(0x881A0B34),
                    radius = r,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = Color(0x38D8B4FE),
                    radius = r - 0.5.dp.toPx(),
                    center = Offset(cx, cy),
                    style = Stroke(width = 0.75.dp.toPx())
                )
                val bw = size.width * 0.42f
                val bh = size.height * 0.48f
                val left = cx - bw / 2f
                val top = cy - bh / 2f + 1.dp.toPx()
                // Crisp white Clipboard body
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(bw, bh),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.2.dp.toPx(), 2.2.dp.toPx())
                )
                // Top clip tab
                val cw = bw * 0.54f
                val ch = 2.6.dp.toPx()
                drawRoundRect(
                    color = Color(0xFF1A0B34),
                    topLeft = Offset(cx - cw / 2f, top - 0.6.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(cw, ch + 1.2.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.2.dp.toPx(), 1.2.dp.toPx())
                )
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(cx - cw * 0.38f, top - 1.2.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(cw * 0.76f, ch),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx(), 1.dp.toPx())
                )
                // 3 dark lines on clipboard
                for (i in 0..2) {
                    val ly = top + bh * (0.42f + i * 0.20f)
                    drawLine(
                        color = Color(0xFF28134A),
                        start = Offset(left + bw * 0.22f, ly),
                        end = Offset(left + bw * 0.78f, ly),
                        strokeWidth = 1.15.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
            "continue" -> {
                // Dark circular outer recess + bright amber/orange rounded-md square badge
                drawCircle(
                    color = Color(0x881A0B34),
                    radius = r,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = Color(0x38D8B4FE),
                    radius = r - 0.5.dp.toPx(),
                    center = Offset(cx, cy),
                    style = Stroke(width = 0.75.dp.toPx())
                )
                val sq = size.minDimension * 0.64f
                val left = cx - sq / 2f
                val top = cy - sq / 2f
                drawIntoCanvas { canvas ->
                    val glowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        color = android.graphics.Color.argb(175, 245, 158, 11)
                        maskFilter = android.graphics.BlurMaskFilter(4.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
                    }
                    canvas.nativeCanvas.drawRoundRect(left, top, left + sq, top + sq, 4.dp.toPx(), 4.dp.toPx(), glowPaint)
                }
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFBBF24), Color(0xFFF97316)),
                        start = Offset(left, top),
                        end = Offset(left + sq, top + sq)
                    ),
                    topLeft = Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(sq, sq),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
                // Crisp opposing arrows/chevrons (><) inside the amber badge
                val chevStroke = 1.5.dp.toPx()
                val leftChev = Path().apply {
                    moveTo(cx - sq * 0.30f, cy - sq * 0.19f)
                    lineTo(cx - sq * 0.08f, cy)
                    lineTo(cx - sq * 0.30f, cy + sq * 0.19f)
                }
                val rightChev = Path().apply {
                    moveTo(cx + sq * 0.30f, cy - sq * 0.19f)
                    lineTo(cx + sq * 0.08f, cy)
                    lineTo(cx + sq * 0.30f, cy + sq * 0.19f)
                }
                drawPath(leftChev, Color.White, style = Stroke(width = chevStroke, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                drawPath(rightChev, Color.White, style = Stroke(width = chevStroke, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
            }
            "more" -> {
                // Dark circular badge with 3 horizontal white dots (•••)
                drawCircle(
                    color = Color(0x881A0B34),
                    radius = r,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = Color(0x38D8B4FE),
                    radius = r - 0.5.dp.toPx(),
                    center = Offset(cx, cy),
                    style = Stroke(width = 0.75.dp.toPx())
                )
                val dotR = 1.75.dp.toPx()
                val spacing = 4.6.dp.toPx()
                for (i in -1..1) {
                    drawCircle(
                        color = Color.White,
                        radius = dotR,
                        center = Offset(cx + i * spacing, cy)
                    )
                }
            }
        }
    }
}

// 100% Vector-Sharp 3D Circular Icon Well for the 3 Bottom Quick-Action Cards matching img_chat_ref_quick_cards
@Composable
fun QuickActionIconWell(type: String, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(34.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension / 2f

        // Translucent deep plum circular well with subtle lavender rim
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xBC2E135A), Color(0xC2180A33))
            ),
            radius = r,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = Color(0x55D8B4FE),
            radius = r - 0.5.dp.toPx(),
            center = Offset(cx, cy),
            style = Stroke(width = 0.85.dp.toPx())
        )

        when (type) {
            "compass" -> {
                // Crisp 8-pointed electric purple/lilac compass star SVG icon
                drawIntoCanvas { canvas ->
                    val glowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        color = android.graphics.Color.argb(185, 168, 85, 247)
                        maskFilter = android.graphics.BlurMaskFilter(4.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
                    }
                    canvas.nativeCanvas.drawCircle(cx, cy, r * 0.54f, glowPaint)
                }
                // 4 diagonal points of the 8-point star
                val diagOuter = r * 0.56f
                val diagWaist = r * 0.12f
                rotate(degrees = 45f, pivot = Offset(cx, cy)) {
                    val diagStar = Path().apply {
                        moveTo(cx, cy - diagOuter)
                        lineTo(cx + diagWaist, cy - diagWaist)
                        lineTo(cx + diagOuter, cy)
                        lineTo(cx + diagWaist, cy + diagWaist)
                        lineTo(cx, cy + diagOuter)
                        lineTo(cx - diagWaist, cy + diagWaist)
                        lineTo(cx - diagOuter, cy)
                        lineTo(cx - diagWaist, cy - diagWaist)
                        close()
                    }
                    drawPath(path = diagStar, color = Color(0xFFA855F7))
                }
                // 4 cardinal points of the 8-point compass star
                val outerL = r * 0.78f
                val waist = r * 0.16f
                val starPath = Path().apply {
                    moveTo(cx, cy - outerL)
                    lineTo(cx + waist, cy - waist)
                    lineTo(cx + outerL, cy)
                    lineTo(cx + waist, cy + waist)
                    lineTo(cx, cy + outerL)
                    lineTo(cx - waist, cy + waist)
                    lineTo(cx - outerL, cy)
                    lineTo(cx - waist, cy - waist)
                    close()
                }
                drawPath(
                    path = starPath,
                    brush = Brush.linearGradient(
                        listOf(Color.White, Color(0xFFE9D5FF), Color(0xFF9333EA))
                    )
                )
                drawCircle(color = Color.White, radius = 1.8.dp.toPx(), center = Offset(cx, cy))
            }
            "bulb" -> {
                // Warm golden glow behind classic teardrop lightbulb
                drawIntoCanvas { canvas ->
                    val glowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        color = android.graphics.Color.argb(185, 250, 204, 21)
                        maskFilter = android.graphics.BlurMaskFilter(4.5.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
                    }
                    canvas.nativeCanvas.drawCircle(cx, cy - 1.5.dp.toPx(), r * 0.42f, glowPaint)
                }
                val bulbCy = cy - r * 0.14f
                // 5 crisp short radiating rays around upper dome
                val rayAngles = listOf(-148f, -118f, -90f, -62f, -32f)
                rayAngles.forEach { deg ->
                    val rad = Math.toRadians(deg.toDouble())
                    val r1 = r * 0.48f
                    val r2 = r * 0.65f
                    drawLine(
                        color = Color(0xFFFDE047),
                        start = Offset(cx + (Math.cos(rad) * r1).toFloat(), bulbCy + (Math.sin(rad) * r1).toFloat()),
                        end = Offset(cx + (Math.cos(rad) * r2).toFloat(), bulbCy + (Math.sin(rad) * r2).toFloat()),
                        strokeWidth = 1.2.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
                // Classic rounded pear/teardrop glass bulb
                val bulbR = r * 0.30f
                val pearPath = Path().apply {
                    addOval(
                        androidx.compose.ui.geometry.Rect(
                            cx - bulbR,
                            bulbCy - bulbR,
                            cx + bulbR,
                            bulbCy + bulbR
                        )
                    )
                    moveTo(cx - bulbR * 0.72f, bulbCy + bulbR * 0.55f)
                    lineTo(cx - bulbR * 0.48f, bulbCy + bulbR * 1.18f)
                    lineTo(cx + bulbR * 0.48f, bulbCy + bulbR * 1.18f)
                    lineTo(cx + bulbR * 0.72f, bulbCy + bulbR * 0.55f)
                    close()
                }
                drawPath(
                    path = pearPath,
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFFBEB), Color(0xFFFDE047), Color(0xFFF59E0B)),
                        center = Offset(cx - bulbR * 0.2f, bulbCy - bulbR * 0.25f),
                        radius = bulbR * 1.4f
                    )
                )
                // Metallic silver-slate screw base matching reference
                drawRoundRect(
                    color = Color(0xFFE2E8F0),
                    topLeft = Offset(cx - bulbR * 0.46f, bulbCy + bulbR * 1.22f),
                    size = androidx.compose.ui.geometry.Size(bulbR * 0.92f, 2.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx(), 1.dp.toPx())
                )
                drawRoundRect(
                    color = Color(0xFF94A3B8),
                    topLeft = Offset(cx - bulbR * 0.32f, bulbCy + bulbR * 1.52f),
                    size = androidx.compose.ui.geometry.Size(bulbR * 0.64f, 1.8.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx(), 1.dp.toPx())
                )
            }
            "pencil" -> {
                // Crisp white linear edit pencil icon
                rotate(degrees = 45f, pivot = Offset(cx, cy)) {
                    val pw = r * 0.34f
                    val ph = r * 0.92f
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(cx - pw / 2f, cy - ph * 0.48f),
                        size = androidx.compose.ui.geometry.Size(pw, ph * 0.78f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.6.dp.toPx(), 1.6.dp.toPx()),
                        style = Stroke(width = 1.65.dp.toPx())
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(cx - pw / 2f, cy - ph * 0.24f),
                        end = Offset(cx + pw / 2f, cy - ph * 0.24f),
                        strokeWidth = 1.5.dp.toPx()
                    )
                    val tipPath = Path().apply {
                        moveTo(cx - pw / 2f, cy + ph * 0.30f)
                        lineTo(cx, cy + ph * 0.56f)
                        lineTo(cx + pw / 2f, cy + ph * 0.30f)
                        close()
                    }
                    drawPath(
                        path = tipPath,
                        color = Color.White,
                        style = Stroke(width = 1.65.dp.toPx(), join = androidx.compose.ui.graphics.StrokeJoin.Round)
                    )
                }
            }
        }
    }
}

@Composable
fun RpAtmosphericBackground() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val bgRequest = remember(context) {
        coil.request.ImageRequest.Builder(context)
            .data(com.example.R.drawable.img_chat_bg_aiden_wisteria)
            .allowRgb565(true)
            .crossfade(false)
            .build()
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF100720))
        )

        coil.compose.AsyncImage(
            model = bgRequest,
            contentDescription = "Purple Stained Glass & Wisteria Gothic Background",
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    bot: BotEntity,
    messages: List<MessageEntity>,
    userSettings: UserSettingsEntity?,
    isSending: Boolean,
    errorMessage: String?,
    keyCharacters: List<KeyCharacter>,
    characterEmotions: List<com.example.data.local.CharacterEmotionEntity> = emptyList(),
    affectionEvents: List<com.example.data.local.AffectionEventEntity> = emptyList(),
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onContinueScene: (() -> Unit)? = null,
    onRegenerate: () -> Unit,
    onEditMessage: (String, String) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onSaveBotProfile: (BotEntity, List<KeyCharacter>) -> Unit,
    onResetChat: (Boolean) -> Unit,
    onDeleteBot: () -> Unit,
    onSpeakText: ((String) -> Unit)? = null,
    isSpeaking: Boolean = false,
    onStopSpeaking: () -> Unit = {},
    onEnsureOpeningMessage: () -> Unit = {},
    onClearError: () -> Unit = {},
    onRetryMessage: ((String) -> Unit)? = null,
    onRetryFromBanner: (() -> Unit)? = null,
    onRewindToMessage: ((String) -> Unit)? = null,
    onCloneBot: (() -> Unit)? = null,
    streamingMsgId: String? = null,
    streamingText: String? = null
) {
    var inputText by remember { mutableStateOf("") }
    var showBotSettings by remember { mutableStateOf(false) }
    var showQuickProfile by remember { mutableStateOf(false) }
    var showBackgroundPicker by remember { mutableStateOf(false) }
    var showPlusToolkit by remember { mutableStateOf(false) }

    var editingMessageId by remember { mutableStateOf<String?>(null) }
    var editingText by remember { mutableStateOf("") }
    var expandedMenuMessageId by remember { mutableStateOf<String?>(null) }
    var expandedUserMenuMessageId by remember { mutableStateOf<String?>(null) }
    var rewindTargetMessageId by remember { mutableStateOf<String?>(null) }
    var isStatsExpanded by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var localSearchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: com.example.ui.viewmodel.EmochiViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    val searchResults by viewModel.searchResults.collectAsState()
    val starredMessages by viewModel.starredMessages.collectAsState()
    var showStarredSheet by remember { mutableStateOf(false) }
    var activeMilestone by remember { mutableStateOf<com.example.data.local.MilestoneEntity?>(null) }
    // Reaction picker state
    var reactionPickerMessageId by remember { mutableStateOf<String?>(null) }
    val reactionEmojis = listOf("❤️", "😂", "😮", "😢", "👍", "🔥")
    val milestoneEvent = viewModel.milestoneEvent

    // Milestone event collector
    LaunchedEffect(Unit) {
        milestoneEvent.collect { milestone ->
            activeMilestone = milestone
            kotlinx.coroutines.delay(4000)
            activeMilestone = null
        }
    }

    // Stop speaking immediately when exiting chat
    DisposableEffect(Unit) {
        onDispose {
            onStopSpeaking()
        }
    }

    // Automatically ensure opening message exists if list is empty (except for book mode)
    val rawValidMessages = remember(messages) {
        messages.filter { it.text.isNotBlank() && !it.id.contains("_ref_msg_") }
    }

    // Close message action pills and reaction picker automatically if the active message scrolls off-screen
    val visibleItemKeys by remember {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo.mapNotNull { it.key as? String }.toSet()
        }
    }
    LaunchedEffect(visibleItemKeys, listState.isScrollInProgress) {
        if (visibleItemKeys.isNotEmpty()) {
            if (expandedMenuMessageId != null && expandedMenuMessageId !in visibleItemKeys) {
                expandedMenuMessageId = null
            }
            if (expandedUserMenuMessageId != null && expandedUserMenuMessageId !in visibleItemKeys) {
                expandedUserMenuMessageId = null
            }
            if (reactionPickerMessageId != null && reactionPickerMessageId !in visibleItemKeys) {
                reactionPickerMessageId = null
            }
        }
    }

    LaunchedEffect(bot.id, rawValidMessages.isEmpty()) {
        if (rawValidMessages.isEmpty() && !isSending && bot.mode != "book") {
            onEnsureOpeningMessage()
        }
    }

    // Scroll to bottom on new messages or streaming updates
    LaunchedEffect(rawValidMessages.size, isSending, streamingText) {
        val hasWaitingItem = isSending && streamingText.isNullOrBlank()
        val totalItems = rawValidMessages.size + if (hasWaitingItem) 1 else 0
        if (totalItems > 0) {
            try {
                listState.animateScrollToItem(totalItems - 1)
            } catch (_: Exception) {}
        }
    }

    val isUniverse = bot.mode == "universe"
    val displayName = if (isUniverse) bot.universeName.ifBlank { bot.aiName.ifBlank { "Evren" } } else bot.aiName.ifBlank { bot.universeName.ifBlank { "Karakter" } }
    val avatarInitial = remember(displayName) {
        displayName.trim().firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString() ?: "A"
    }

    // --- Rich Animations Setup ---
    val infiniteTransition = rememberInfiniteTransition(label = "chatScreenAnimations")

    // 1. Breathing Glow Alpha Pulse (0.35f to 0.90f)
    val pulseGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.90f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlowAlpha"
    )

    // 2. Tactile Pulse Scale (0.96f to 1.04f)
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // 3. Rotating Sweep Angle for Neon Borders (0f to 360f)
    val sweepRotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepRotateAngle"
    )

    // Mood detection based on last AI text
    val lastAiText = rawValidMessages.lastOrNull { it.role == "assistant" }?.text ?: ""
    val detectedMood = remember(lastAiText) {
        val lower = lastAiText.lowercase()
        when {
            listOf("harika", "süper", "mutlu", "güldü", "🎉", "😊").any { lower.contains(it) } -> "joy"
            listOf("canım", "değerlisin", "sarıl", "güzelim", "sevgi", "💕", "❤️").any { lower.contains(it) } -> "warm"
            listOf("üzgün", "üzülüyorum", "kötü", "yalnız", "😢").any { lower.contains(it) } -> "sad"
            listOf("tehlike", "sinirli", "öfke", "korktu").any { lower.contains(it) } -> "tense"
            listOf("merak", "acaba", "ilginç").any { lower.contains(it) } -> "curious"
            else -> "calm"
        }
    }

    val hue = MoodColors.getMoodHue(detectedMood)
    val emotionState = remember(bot.emotionState) { com.example.data.local.EmotionState.fromJson(bot.emotionState) }
    val realMoodDisplay = "${emotionState.getMoodEmoji()} ${emotionState.mood.replaceFirstChar { it.uppercase() }}"

    val lastAiIndex = rawValidMessages.indexOfLast { it.role == "assistant" }

    val isBookMode = remember(bot) {
        bot.mode == "book" ||
        bot.id == "preset_aiden_mcu_cosmic" ||
        bot.aiName.contains("Kitap") ||
        bot.aiPersonality.contains("KİTAP MODU") ||
        bot.scenario.contains("KİTAP MODU")
    }

    if (isBookMode) {
        BookReaderView(
            bot = bot,
            messages = rawValidMessages,
            isSending = isSending,
            errorMessage = errorMessage,
            onBack = onBack,
            onSendMessage = onSendMessage,
            onEnsureOpeningMessage = onEnsureOpeningMessage,
            onResetChat = { onResetChat(it) },
            onClearError = onClearError
        )
    } else {
    val rootSizeState = remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    val rootPosState = remember { mutableStateOf(Offset.Zero) }
    val rootSize = rootSizeState.value
    val blurredBgBitmap = remember(context) { createFrostedWisteriaBackdropBitmap(context) }
    val refGlassBitmaps = remember(context) { loadAetheriaRefGlassBitmaps(context) }
    val headerTitle = displayName
    val validMessages = rawValidMessages

    androidx.compose.runtime.CompositionLocalProvider(
        LocalAetheriaRootPos provides rootPosState,
        LocalAetheriaRootSize provides rootSizeState
    ) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { rootSizeState.value = it }
            .onGloballyPositioned {
                if (it.isAttached) {
                    rootPosState.value = it.positionInRoot()
                }
            }
    ) {
        val isImeVisible = WindowInsets.isImeVisible
        val density = androidx.compose.ui.platform.LocalDensity.current

        // When keyboard opens: smoothly scroll chat to the bottom
        LaunchedEffect(isImeVisible) {
            if (isImeVisible) {
                val activeId = reactionPickerMessageId ?: expandedMenuMessageId ?: expandedUserMenuMessageId
                if (activeId == null) {
                    val hasWaitingItem = isSending && streamingText.isNullOrBlank()
                    val totalItems = rawValidMessages.size + if (hasWaitingItem) 1 else 0
                    if (totalItems > 0) {
                        kotlinx.coroutines.delay(90)
                        try {
                            listState.animateScrollToItem(totalItems - 1)
                        } catch (_: Exception) {}
                    }
                }
            }
        }

        // Whenever [Kopyala / Devam Et / Diğer] or the Reaction Picker opens (or keyboard opens while one is open),
        // gently scroll down so the newly opened pills/reactions below the message are 100% visible!
        LaunchedEffect(expandedMenuMessageId, expandedUserMenuMessageId, reactionPickerMessageId, isImeVisible) {
            val targetId = reactionPickerMessageId ?: expandedMenuMessageId ?: expandedUserMenuMessageId ?: return@LaunchedEffect
            kotlinx.coroutines.delay(110)
            try {
                val layoutInfo = listState.layoutInfo
                val itemInfo = layoutInfo.visibleItemsInfo.find { it.key == targetId }
                if (itemInfo != null) {
                    val visibleBottom = layoutInfo.viewportEndOffset - layoutInfo.afterContentPadding
                    val itemBottom = itemInfo.offset + itemInfo.size
                    val extraAllowancePx = with(density) { 58.dp.toPx() }
                    val overflowPx = (itemBottom + extraAllowancePx) - visibleBottom
                    if (overflowPx > 0f) {
                        listState.animateScrollBy(
                            value = overflowPx,
                            animationSpec = tween(260, easing = FastOutSlowInEasing)
                        )
                    } else if (itemBottom > visibleBottom * 0.55f) {
                        // Even if not overflowing, nudge down slightly so the action bar sits comfortably in view
                        listState.animateScrollBy(
                            value = with(density) { 38.dp.toPx() },
                            animationSpec = tween(240, easing = FastOutSlowInEasing)
                        )
                    }
                }
            } catch (_: Exception) {}
        }

        // Base atmospheric wallpaper (clean seamless wisteria gothic cathedral background)
        RpAtmosphericBackground()

        // Custom wallpaper overlay only when explicitly customized with a custom image path/URL
        if (bot.chatBgUrl.isNotBlank() && bot.chatBgUrl != "USE_AVATAR" && bot.id != "starter_aetheria" && bot.id != "starter_aiden_stories") {
            coil.compose.AsyncImage(
                model = bot.chatBgUrl,
                contentDescription = "Sohbet Arka Planı",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.85f
            )
        }

        Scaffold(
            modifier = Modifier.graphicsLayer {
                alpha = if (showBotSettings) 0f else 1f
            },
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                // ── SECTION 2: ÜST BAR (HEADER) — 1:1 REFERENCE BLURRED BANNER & SIGIL OVER SEE-THROUGH WISTERIA ──
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(top = 4.dp, bottom = 6.dp)
                            .animateContentSize(animationSpec = tween(220, easing = FastOutSlowInEasing)),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(841f / 136f)
                        ) {
                            val headerW = maxWidth
                            val headerH = maxHeight

                            // 1:1 Reference header plate (softly blurred perimeter around bot name + Evrenler, no purple checkmark)
                            Canvas(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                if (refGlassBitmaps.headerNoLetter.width > 1) {
                                    drawImage(
                                        image = refGlassBitmaps.headerNoLetter,
                                        dstOffset = androidx.compose.ui.unit.IntOffset.Zero,
                                        dstSize = androidx.compose.ui.unit.IntSize(
                                            kotlin.math.round(size.width).toInt().coerceAtLeast(1),
                                            kotlin.math.round(size.height).toInt().coerceAtLeast(1)
                                        ),
                                        filterQuality = FilterQuality.High
                                    )
                                }
                            }

                            // Crisp custom avatar or metallic serif initial ("A") centered inside the cosmic sigil medallion (x = 181/841, y = 68/136)
                            Box(
                                modifier = Modifier
                                    .offset(
                                        x = headerW * (145f / 841f),
                                        y = headerH * (30f / 136f)
                                    )
                                    .size(
                                        width = headerW * (72f / 841f),
                                        height = headerH * (76f / 136f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                val fallbackInitial: @Composable () -> Unit = {
                                    Text(
                                        text = avatarInitial,
                                        fontSize = 23.sp,
                                        fontFamily = DisplaySerifFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        style = TextStyle(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.White,
                                                    Color(0xFFF3E8FF),
                                                    Color(0xFFD8B4FE)
                                                )
                                            ),
                                            shadow = Shadow(
                                                color = Color(0xCCA855F7),
                                                offset = Offset(0f, 0f),
                                                blurRadius = 10f
                                            )
                                        )
                                    )
                                }
                                if (bot.avatarUrl.isNotBlank()) {
                                    com.example.ui.components.BotAvatarImage(
                                        avatarUrl = bot.avatarUrl,
                                        contentDescription = headerTitle,
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier
                                            .size(headerW * (54f / 841f))
                                            .clip(CircleShape),
                                        fallback = fallbackInitial
                                    )
                                } else {
                                    fallbackInitial()
                                }
                            }

                            // Bot Title ("Aiden Blackwood Stories") centered above "──── ✦ Evrenler ✦ ────" (NO purple checkmark!)
                            Box(
                                modifier = Modifier
                                    .offset(
                                        x = headerW * (248f / 841f),
                                        y = headerH * (24f / 136f)
                                    )
                                    .size(
                                        width = headerW * (360f / 841f),
                                        height = headerH * (46f / 136f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = headerTitle,
                                    color = Color(0xFFFBF8FF),
                                    fontSize = if (headerTitle.length > 20) 14.5.sp else 15.5.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = LiterarySerifFontFamily,
                                    letterSpacing = 0.1.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    style = TextStyle(
                                        shadow = Shadow(
                                            color = Color(0xCC120524),
                                            offset = Offset(0f, 1.5f),
                                            blurRadius = 6f
                                        )
                                    )
                                )
                            }

                            // Interactive Touch Targets aligned 1:1 with the 4 header elements (<, Bot Banner, 🔍, ⚙️)
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 2.1 Sol Geri (<) — Navigating back to Bot List closes any open message menus/reactions
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(0.145f)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            expandedMenuMessageId = null
                                            expandedUserMenuMessageId = null
                                            reactionPickerMessageId = null
                                            onBack()
                                        }
                                )
                                // 2.2 Orta Avatar + Bot Başlık Kapsülü
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(0.595f)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) { showBotSettings = true }
                                )
                                // 2.3 Sağ Arama (🔍)
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(0.125f)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            showSearchBar = !showSearchBar
                                            if (!showSearchBar) {
                                                localSearchQuery = ""
                                                viewModel.clearSearch()
                                            }
                                        }
                                )
                                // 2.4 Sağ Ayarlar (⚙️)
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(0.135f)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) { showBotSettings = true }
                                        .testTag("bot_settings_button")
                                )
                            }
                        }

                        // Search bar (slide-in) — Mor Cam Frosted Capsule
                        AnimatedVisibility(
                            visible = showSearchBar,
                            enter = slideInVertically(tween(220, easing = FastOutSlowInEasing)) { -it } + fadeIn(tween(220)),
                            exit = slideOutVertically(tween(180)) { -it } + fadeOut(tween(180))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MorCamGlassTextField(
                                    value = localSearchQuery,
                                    onValueChange = { q ->
                                        localSearchQuery = q
                                        viewModel.setSearchQuery(q.ifBlank { null })
                                    },
                                    placeholder = "Mesajlarda ara...",
                                    leadingIcon = Icons.Default.Search,
                                    singleLine = true,
                                    minHeight = 46.dp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .aetheriaBackdropBlur(rootSize, blurredBgBitmap, alpha = 0.55f),
                                    trailingContent = if (localSearchQuery.isNotEmpty()) {
                                        {
                                            MorCamCircleIconButton(
                                                icon = Icons.Default.Close,
                                                contentDescription = "Temizle",
                                                onClick = {
                                                    localSearchQuery = ""
                                                    viewModel.clearSearch()
                                                },
                                                size = 36.dp
                                            )
                                        }
                                    } else null
                                )
                            }
                        }

                        // Search results summary
                        AnimatedVisibility(
                            visible = showSearchBar && searchResults.isNotEmpty(),
                            enter = fadeIn(tween(200)),
                            exit = fadeOut(tween(150))
                        ) {
                            Text(
                                text = "${searchResults.size} sonuç bulundu",
                                color = MorCamTokens.LavenderSoft,
                                fontSize = 11.5.sp,
                                fontFamily = LiterarySerifFontFamily,
                                modifier = Modifier.padding(start = 18.dp, bottom = 4.dp)
                            )
                        }

                        AnimatedVisibility(
                            visible = isSpeaking && !isImeVisible,
                            enter = fadeIn(tween(200)) + expandVertically(tween(200)),
                            exit = fadeOut(tween(160)) + shrinkVertically(tween(160))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 4.dp)
                                    .clip(RoundedCornerShape(50))
                                    .aetheriaBackdropBlur(rootSize, blurredBgBitmap, alpha = 0.62f)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xA6200D44), Color(0xB814082C), Color(0xA6200D44))
                                        )
                                    )
                                    .border(BorderStroke(1.dp, MorCamTokens.RimGradient), RoundedCornerShape(50))
                                    .padding(start = 14.dp, end = 6.dp, top = 5.dp, bottom = 5.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = null,
                                            tint = MorCamTokens.LavenderLight,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Sesli okunuyor...",
                                            color = MorCamTokens.SilverLavenderBright,
                                            fontFamily = LiterarySerifFontFamily,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    MorCamChipPill(
                                        label = "Durdur",
                                        selected = true,
                                        icon = Icons.Default.Stop,
                                        onClick = { onStopSpeaking() }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                // ── SECTION 5 & SECTION 6: ALT HIZLI ÖNERİ KARTLARI + ALT MESAJ YAZMA BARI (1:1 REFERENCE PLATES WITH SEE-THROUGH GLASS) ──
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                            .padding(top = 4.dp, bottom = 4.dp)
                    ) {
                        // Interactive (+) RP & Sahne Araçları Drawer
                        AnimatedVisibility(
                            visible = showPlusToolkit,
                            enter = fadeIn(tween(180)) + expandVertically(tween(180)),
                            exit = fadeOut(tween(140)) + shrinkVertically(tween(140))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val tools = listOf(
                                    "⏩ Devam Et" to {
                                        showPlusToolkit = false
                                        if (!isSending) {
                                            if (onContinueScene != null) onContinueScene()
                                            else viewModel.continueStoryScene()
                                        }
                                    },
                                    "✨ Sahneyi Derinleştir" to {
                                        showPlusToolkit = false
                                        if (!isSending) {
                                            onSendMessage("Mevcut sahneyi kesmeden ortam atmosferini, karakterin iç sesini ve duygusal gerilimi edebi olarak derinleştir.")
                                        }
                                    },
                                    "💡 Ne Yapmalıyım?" to {
                                        showPlusToolkit = false
                                        if (!isSending) {
                                            onSendMessage("Şu anki roman kurgusunda yapabileceğim 3 farklı dramatik hamle (1. Karanlık/Riskli hamle, 2. Tutkulu/Duygusal hamle, 3. Stratejik hamle) öner ve sahneyi sürdür.")
                                        }
                                    },
                                    "✨ *Eylem Ekle*" to {
                                        val t = inputText.trim()
                                        inputText = if (t.isBlank()) "*...*" else if (t.startsWith("*") && t.endsWith("*")) t.trim('*') else "*$t*"
                                        showPlusToolkit = false
                                    },
                                    "🎲 Kader Zarı" to {
                                        showPlusToolkit = false
                                        if (!isSending) onSendMessage("*Beklenmedik bir gelişme yaşanır ve sahnenin dengesi aniden değişir.*")
                                    },
                                    "⏳ Zaman Atla" to {
                                        showPlusToolkit = false
                                        if (!isSending) onSendMessage("*Aradan biraz zaman geçer; sahne bir sonraki kritik ana ilerler.*")
                                    },
                                    "🌌 Ortamı Betimle" to {
                                        showPlusToolkit = false
                                        if (!isSending) onSendMessage("Lütfen şu an bulunduğumuz ortamı, ışığı, sesleri ve atmosferi tüm duyularla hissettirecek şekilde betimle.")
                                    },
                                    "🧠 İç Ses" to {
                                        showPlusToolkit = false
                                        if (!isSending) onSendMessage("Karakterin şu an dışarıya belli etmediği gerçek iç düşüncelerini ve duygularını hissettirerek devam et.")
                                    }
                                )
                                tools.forEach { (label, action) ->
                                    Box(
                                        modifier = Modifier
                                            .height(36.dp)
                                            .morCamOuterGlow(cornerRadius = 18.dp, shadowAlpha = 0.28f, rimGlowAlpha = 0.26f, bottomFlareAlpha = 0.56f)
                                            .clip(RoundedCornerShape(50))
                                            .aetheriaBackdropBlur(rootSize, blurredBgBitmap, alpha = 0.65f)
                                            .morCamGlassSurface(
                                                baseSmokeColor = Color(0x2816072C),
                                                topRightFrostAlpha = 0.20f,
                                                topLeftFrostAlpha = 0.12f,
                                                topStarFrostAlpha = 0f,
                                                bottomEdgeRefractionAlpha = 0.18f
                                            )
                                            .border(
                                                BorderStroke(0.9.dp, Color(0xCCE9D5FF)),
                                                RoundedCornerShape(50)
                                            )
                                            .morCamTopAndBottomSpecular(cornerRadius = 18.dp, topSheenAlpha = 0.24f, bottomFlareAlpha = 0.60f)
                                            .clickable { action() }
                                            .padding(horizontal = 14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            color = Color.White,
                                            fontSize = 12.5.sp,
                                            fontFamily = LiterarySerifFontFamily
                                        )
                                    }
                                }
                            }
                        }

                        // ── SECTION 5: ALT HIZLI ÖNERİ KARTLARI (1:1 Reference Plate with Translucent See-Through Glass) ──
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(841f / 138f)
                        ) {
                            Canvas(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                if (refGlassBitmaps.quickCards.width > 1) {
                                    drawImage(
                                        image = refGlassBitmaps.quickCards,
                                        dstOffset = androidx.compose.ui.unit.IntOffset.Zero,
                                        dstSize = androidx.compose.ui.unit.IntSize(
                                            kotlin.math.round(size.width).toInt().coerceAtLeast(1),
                                            kotlin.math.round(size.height).toInt().coerceAtLeast(1)
                                        ),
                                        filterQuality = FilterQuality.High
                                    )
                                }
                            }

                            // 3 Touch Targets for [Sahneyi Derinleştir] [Ne Yapmalıyım?] [Düzenle]
                            Row(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Card 1: Sahneyi Derinleştir (x = 29..300)
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(0.36f)
                                        .clickable(
                                            enabled = !isSending,
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            onSendMessage("Mevcut sahneyi kesmeden ortam atmosferini, karakterin iç sesini ve duygusal gerilimi edebi olarak derinleştir.")
                                        }
                                )
                                // Card 2: Ne Yapmalıyım? (x = 307..580)
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(0.33f)
                                        .clickable(
                                            enabled = !isSending,
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            onSendMessage("Şu anki roman kurgusunda yapabileceğim 3 farklı dramatik hamle (1. Karanlık/Riskli hamle, 2. Tutkulu/Duygusal hamle, 3. Stratejik hamle) öner ve sahneyi sürdür.")
                                        }
                                )
                                // Card 3: Düzenle (x = 587..822) — Disabled until message generation finishes!
                                val canEditLastMessage = !isSending && streamingMsgId == null && validMessages.isNotEmpty()
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(0.31f)
                                        .clickable(
                                            enabled = canEditLastMessage,
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            val targetMsg = validMessages.lastOrNull()
                                            if (targetMsg != null) {
                                                editingMessageId = targetMsg.id
                                                editingText = targetMsg.text
                                            }
                                        }
                                )
                            }
                        }

                        // ── SECTION 6: ALT MESAJ YAZMA BARI (1:1 Reference Plate with Translucent See-Through Glass) ──
                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(841f / 138f)
                        ) {
                            val barW = maxWidth
                            val barH = maxHeight

                            Canvas(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                if (refGlassBitmaps.inputBarNoText.width > 1) {
                                    drawImage(
                                        image = refGlassBitmaps.inputBarNoText,
                                        dstOffset = androidx.compose.ui.unit.IntOffset.Zero,
                                        dstSize = androidx.compose.ui.unit.IntSize(
                                            kotlin.math.round(size.width).toInt().coerceAtLeast(1),
                                            kotlin.math.round(size.height).toInt().coerceAtLeast(1)
                                        ),
                                        filterQuality = FilterQuality.High
                                    )
                                }
                            }

                            // 6.1 Sol (+) Butonu (x = 0..145/841)
                            Box(
                                modifier = Modifier
                                    .offset(x = barW * (22f / 841f), y = barH * (16f / 138f))
                                    .size(width = barW * (124f / 841f), height = barH * (106f / 138f))
                                    .clip(CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        showPlusToolkit = !showPlusToolkit
                                    }
                            )

                            // 6.2 Orta Mesaj Giriş Alanı ("Bir mesaj yazın...") at x = 178..582/841
                            Box(
                                modifier = Modifier
                                    .offset(x = barW * (178f / 841f), y = barH * (26f / 138f))
                                    .size(width = barW * (402f / 841f), height = barH * (86f / 138f)),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (inputText.isEmpty()) {
                                    Text(
                                        text = "Bir mesaj yazın...",
                                        color = Color(0xFFE9D5FF).copy(alpha = 0.85f),
                                        fontSize = 15.sp,
                                        fontFamily = LiterarySerifFontFamily
                                    )
                                }
                                androidx.compose.foundation.text.BasicTextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    textStyle = TextStyle(
                                        color = Color.White,
                                        fontSize = 15.5.sp,
                                        fontFamily = LiterarySerifFontFamily
                                    ),
                                    cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE9D5FF)),
                                    maxLines = 2,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("chat_input_field")
                                )
                            }

                            // 6.3 Sağ İç Ataş (📎) Butonu at x = 586..675/841
                            Box(
                                modifier = Modifier
                                    .offset(x = barW * (586f / 841f), y = barH * (24f / 138f))
                                    .size(width = barW * (88f / 841f), height = barH * (90f / 138f))
                                    .clip(CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        showBackgroundPicker = true
                                    }
                            )

                            // 6.4 Sağ Gönder (➤) Butonu at x = 695..825/841 — Does nothing when input is blank!
                            val canSendNow = !isSending && inputText.trim().isNotEmpty()
                            Box(
                                modifier = Modifier
                                    .offset(x = barW * (695f / 841f), y = barH * (12f / 138f))
                                    .size(width = barW * (130f / 841f), height = barH * (114f / 138f))
                                    .clip(CircleShape)
                                    .clickable(
                                        enabled = canSendNow,
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        val text = inputText.trim()
                                        if (text.isNotBlank() && !isSending) {
                                            inputText = ""
                                            onSendMessage(text)
                                        }
                                    }
                                    .testTag("send_message_button")
                            )
                        }
                    }
                }
            }
    ) { innerPadding ->
        val topInsetPadding = innerPadding.calculateTopPadding()
        val bottomInsetPadding = innerPadding.calculateBottomPadding()

        // Keep chat anchored to the latest message whenever the keyboard (IME) expands the bottom inset
        LaunchedEffect(bottomInsetPadding, isImeVisible) {
            if (isImeVisible) {
                val hasWaitingItem = isSending && streamingText.isNullOrBlank()
                val totalItems = rawValidMessages.size + if (hasWaitingItem) 1 else 0
                if (totalItems > 0) {
                    try {
                        listState.scrollToItem(totalItems - 1)
                    } catch (_: Exception) {}
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                errorMessage?.let { err ->
                    androidx.compose.animation.AnimatedVisibility(
                        visible = true,
                        modifier = Modifier.padding(top = topInsetPadding),
                        enter = androidx.compose.animation.slideInVertically(
                            animationSpec = tween(280, easing = FastOutSlowInEasing)
                        ) { -it } + fadeIn(tween(200)),
                        exit = androidx.compose.animation.slideOutVertically(
                            animationSpec = tween(220)
                        ) { -it } + fadeOut(tween(160))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xCC3D1018), Color(0xCC1F0A10))
                                    )
                                )
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        Brush.linearGradient(
                                            listOf(Color(0xFFF87171), Color(0xFFDC2626), Color(0xFFF87171))
                                        )
                                    ),
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x30F87171)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = null,
                                            tint = EmochiError,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = err,
                                        color = EmochiError,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (onRetryFromBanner != null) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0x40F87171))
                                                .border(1.dp, Color(0x60F87171), RoundedCornerShape(8.dp))
                                                .clickable { onRetryFromBanner(); onClearError() }
                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Text(
                                                text = "Tekrar",
                                                color = EmochiError,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x25F87171))
                                            .clickable { onClearError() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Kapat",
                                            tint = EmochiError,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Message region with a rich, multi-stop atmospheric fade under the top header bar and bottom bar!
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        top = (topInsetPadding + 8.dp).coerceAtLeast(88.dp),
                        bottom = (bottomInsetPadding + 14.dp).coerceAtLeast(144.dp)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .graphicsLayer {
                            compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen
                        }
                        .drawWithContent {
                            drawContent()
                            val h = size.height
                            if (h > 10f) {
                                // Top multi-stop smooth fade so scrolling messages never clash with the status bar or top header
                                val topClearEnd = ((topInsetPadding - 22.dp).toPx().coerceAtLeast(0f) / h).coerceIn(0f, 0.25f)
                                val topFadeMid1 = ((topInsetPadding - 4.dp).toPx().coerceAtLeast(0f) / h).coerceIn(topClearEnd + 0.005f, 0.30f)
                                val topFadeMid2 = ((topInsetPadding + 14.dp).toPx().coerceAtLeast(0f) / h).coerceIn(topFadeMid1 + 0.005f, 0.35f)
                                val topFadeEnd = ((topInsetPadding + 36.dp).toPx().coerceAtLeast(0f) / h).coerceIn(topFadeMid2 + 0.005f, 0.42f)

                                // Bottom smooth feather into the quick-action cards
                                val botFadeStart = ((h - (bottomInsetPadding + 22.dp).toPx()) / h).coerceIn(0.55f, 0.94f)
                                val botFadeEnd = ((h - (bottomInsetPadding - 10.dp).toPx()) / h).coerceIn(botFadeStart + 0.005f, 0.99f)

                                drawRect(
                                    brush = Brush.verticalGradient(
                                        0.0f to Color.Transparent,
                                        topClearEnd to Color.Transparent,
                                        topFadeMid1 to Color.Black.copy(alpha = 0.28f),
                                        topFadeMid2 to Color.Black.copy(alpha = 0.68f),
                                        topFadeEnd to Color.Black,
                                        botFadeStart to Color.Black,
                                        botFadeEnd to Color.Black.copy(alpha = 0.18f),
                                        1.0f to Color.Transparent
                                    ),
                                    blendMode = BlendMode.DstIn
                                )
                            }
                        },
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(validMessages, key = { _, msg -> msg.id }) { index, msg ->
                        val isUser = msg.role == "user"

                        val timeFormatted = remember(msg.timestamp) {
                            if (msg.timestamp > 0L) {
                                val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                                sdf.format(java.util.Date(msg.timestamp))
                            } else ""
                        }

                        if (editingMessageId == msg.id) {
                            MorCamGlassCard(
                                modifier = Modifier
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                                    .aetheriaBackdropBlur(rootSize, blurredBgBitmap, alpha = 0.62f),
                                cornerRadius = 20.dp,
                                highlighted = true,
                                paddingHorizontal = 14.dp,
                                paddingVertical = 14.dp
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(bottom = 10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Brush.radialGradient(listOf(Color(0x888B5CF6), Color(0x553B1470))))
                                            .border(0.9.dp, MorCamTokens.RimGradient, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = MorCamTokens.SilverLavenderBright,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Text(
                                        text = "Mesajı Düzenle",
                                        color = MorCamTokens.SilverLavenderBright,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = LiterarySerifFontFamily
                                    )
                                }
                                MorCamGlassTextField(
                                    value = editingText,
                                    onValueChange = { editingText = it },
                                    placeholder = "Mesaj metnini düzenle...",
                                    singleLine = false,
                                    minHeight = 76.dp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    MorCamCapsuleButton(
                                        text = "Vazgeç",
                                        onClick = { editingMessageId = null },
                                        variant = MorCamButtonVariant.Secondary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    MorCamCapsuleButton(
                                        text = "Kaydet",
                                        onClick = {
                                            val text = editingText
                                            editingMessageId = null
                                            onEditMessage(msg.id, text)
                                        },
                                        variant = MorCamButtonVariant.Primary,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        } else if (isUser) {
                            // ── SECTION 3.1: SAĞ HİZALI KULLANICI MESAJ BALONU (3-SLICE AUTHENTIC REFERENCE PLATE + TRANSLUCENT FROSTED GLASS) ──
                            val userInteraction = remember { MutableInteractionSource() }
                            val isUserPressed by userInteraction.collectIsPressedAsState()
                            val userScale by animateFloatAsState(
                                targetValue = if (isUserPressed) 0.985f else 1f,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                label = "userBubbleScale_${msg.id}"
                            )
                            var showSecondaryUserActions by remember(msg.id) { mutableStateOf(false) }
                            val userPillsScrollState = rememberScrollState()

                            LaunchedEffect(showSecondaryUserActions) {
                                if (showSecondaryUserActions) {
                                    kotlinx.coroutines.delay(60)
                                    userPillsScrollState.animateScrollTo(260)
                                }
                            }

                            // Two ticks (✓✓) if the bot has started writing or already replied after this user message; single tick (✓) otherwise
                            val hasBotStartedReplying = remember(validMessages, index, isSending, streamingText) {
                                validMessages.drop(index + 1).any { it.role == "assistant" && it.text.isNotBlank() } ||
                                    (isSending && index == validMessages.lastIndex && !streamingText.isNullOrBlank())
                            }

                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        scaleX = userScale
                                        scaleY = userScale
                                    }
                                    .aetheriaThreeSliceGlassBubble(
                                        rootSize = rootSize,
                                        blurredBitmap = blurredBgBitmap,
                                        topSlice = refGlassBitmaps.userTop,
                                        midSlice = refGlassBitmaps.userMid,
                                        bottomSlice = refGlassBitmaps.userBottom,
                                        isUserBubble = true,
                                        frostedAlpha = 0.62f
                                    )
                                    .combinedClickable(
                                        interactionSource = userInteraction,
                                        indication = null,
                                        onClick = {
                                            if (reactionPickerMessageId != null) {
                                                reactionPickerMessageId = null
                                            } else {
                                                val willOpen = expandedUserMenuMessageId != msg.id
                                                expandedUserMenuMessageId = if (willOpen) msg.id else null
                                                if (!willOpen) showSecondaryUserActions = false
                                            }
                                        },
                                        onLongClick = {
                                            expandedUserMenuMessageId = null
                                            expandedMenuMessageId = null
                                            reactionPickerMessageId = if (reactionPickerMessageId == msg.id) null else msg.id
                                        }
                                    )
                            ) {
                                val uW = maxWidth
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = uW * (108f / 841f))
                                        .padding(
                                            start = uW * (204f / 841f),
                                            end = uW * (48f / 841f),
                                            top = 21.dp,
                                            bottom = 14.dp
                                        )
                                ) {
                                    Text(
                                        text = formatNarrativeText(cleanCopyText(msg.text)),
                                        color = Color.White.copy(alpha = 0.97f),
                                        fontSize = 15.sp,
                                        lineHeight = 22.5.sp,
                                        letterSpacing = 0.1.sp,
                                        fontFamily = LiterarySerifFontFamily,
                                        style = TextStyle(
                                            shadow = Shadow(
                                                color = Color(0x88120524),
                                                offset = Offset(0f, 1f),
                                                blurRadius = 3.5f
                                            )
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = timeFormatted.ifBlank { "22:47" },
                                            color = Color(0xEBF8F5FF),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Normal,
                                            fontFamily = InterFontFamily
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        if (hasBotStartedReplying) {
                                            DoubleCheckmarkIcon(tint = Color(0xEBFFFFFF))
                                        } else {
                                            SingleCheckmarkIcon(tint = Color(0xEBFFFFFF))
                                        }
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.End
                            ) {

                                // ── User Message Action Capsules (Smooth horizontal edge-fade transition on left & right!) ──
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = expandedUserMenuMessageId == msg.id && reactionPickerMessageId == null,
                                    enter = fadeIn(tween(180)) + expandVertically(tween(180)),
                                    exit = fadeOut(tween(140)) + shrinkVertically(tween(140))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .graphicsLayer {
                                                compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen
                                            }
                                            .drawWithContent {
                                                drawContent()
                                                drawRect(
                                                    brush = Brush.horizontalGradient(
                                                        0.0f to Color.Transparent,
                                                        0.05f to Color.Black,
                                                        0.95f to Color.Black,
                                                        1.0f to Color.Transparent
                                                    ),
                                                    blendMode = BlendMode.DstIn
                                                )
                                            }
                                            .horizontalScroll(userPillsScrollState)
                                            .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        @Composable
                                        fun UserGlassActionPill(
                                            iconType: String? = null,
                                            label: String,
                                            isDanger: Boolean = false,
                                            isActive: Boolean = false,
                                            onClick: () -> Unit
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .height(40.dp)
                                                    .morCamOuterGlow(cornerRadius = 20.dp, shadowAlpha = 0.34f, rimGlowAlpha = 0.24f, bottomFlareAlpha = 0.42f)
                                                    .clip(RoundedCornerShape(50))
                                                    .aetheriaBackdropBlur(rootSize, blurredBgBitmap, alpha = 0.85f)
                                                    .morCamGlassSurface(
                                                        baseSmokeColor = when {
                                                            isDanger -> Color(0x603E0E1A)
                                                            isActive -> Color(0x664C1D95)
                                                            else -> Color(0x52160A2B)
                                                        },
                                                        topRightFrostAlpha = 0.22f,
                                                        topLeftFrostAlpha = 0.18f,
                                                        topStarFrostAlpha = 0f,
                                                        bottomEdgeRefractionAlpha = 0.18f,
                                                        cornerRadius = 20.dp
                                                    )
                                                    .border(
                                                        BorderStroke(
                                                            0.9.dp,
                                                            if (isDanger) {
                                                                Brush.linearGradient(listOf(Color(0xCCF87171), Color(0x99EF4444)))
                                                            } else {
                                                                Brush.linearGradient(
                                                                    listOf(Color(0xCC9F70E6), Color(0x997B4BC2), Color(0xCCA879EC))
                                                                )
                                                            }
                                                        ),
                                                        RoundedCornerShape(50)
                                                    )
                                                    .morCamTopAndBottomSpecular(cornerRadius = 20.dp, topSheenAlpha = 0.22f, bottomFlareAlpha = 0.44f)
                                                    .clickable { onClick() }
                                                    .padding(start = if (iconType != null) 7.dp else 14.dp, end = 16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                if (iconType != null) {
                                                    GlassPillIconBadge(type = iconType, modifier = Modifier.size(26.dp))
                                                }
                                                Text(
                                                    text = label,
                                                    color = if (isDanger) Color(0xFFFCA5A5) else Color(0xFFFBF8FF),
                                                    fontSize = 13.sp,
                                                    fontFamily = LiterarySerifFontFamily
                                                )
                                            }
                                        }

                                        UserGlassActionPill(iconType = "copy", label = "Kopyala") {
                                            val cleanText = cleanCopyText(msg.text)
                                            clipboardManager.setText(AnnotatedString(cleanText))
                                            android.widget.Toast.makeText(context, "Kopyalandı", android.widget.Toast.LENGTH_SHORT).show()
                                        }

                                        UserGlassActionPill(iconType = "continue", label = "Devam Et") {
                                            if (!isSending) {
                                                expandedUserMenuMessageId = null
                                                if (onContinueScene != null) onContinueScene()
                                                else viewModel.continueStoryScene()
                                            }
                                        }

                                        UserGlassActionPill(
                                            iconType = "more",
                                            label = "Diğer",
                                            isActive = showSecondaryUserActions
                                        ) {
                                            showSecondaryUserActions = !showSecondaryUserActions
                                        }

                                        // Secondary options expand HORIZONTALLY to the right on the exact same row!
                                        androidx.compose.animation.AnimatedVisibility(
                                            visible = showSecondaryUserActions,
                                            enter = androidx.compose.animation.fadeIn(tween(180)) +
                                                androidx.compose.animation.expandHorizontally(tween(220, easing = FastOutSlowInEasing)),
                                            exit = androidx.compose.animation.fadeOut(tween(140)) +
                                                androidx.compose.animation.shrinkHorizontally(tween(180))
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                UserGlassActionPill(label = "✏️ Düzenle") {
                                                    editingMessageId = msg.id
                                                    editingText = msg.text
                                                    expandedUserMenuMessageId = null
                                                    showSecondaryUserActions = false
                                                }
                                                UserGlassActionPill(label = "⏪ Buraya Sar") {
                                                    rewindTargetMessageId = msg.id
                                                    expandedUserMenuMessageId = null
                                                    showSecondaryUserActions = false
                                                }
                                                UserGlassActionPill(label = "🗑️ Sil", isDanger = true) {
                                                    expandedUserMenuMessageId = null
                                                    showSecondaryUserActions = false
                                                    onDeleteMessage(msg.id)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // ── SECTION 3.2: SOL HİZALI BOT / HİKAYE MESAJ BALONLARI (3-SLICE AUTHENTIC REFERENCE PLATE + TRANSLUCENT FROSTED GLASS) ──
                            val isThisStreaming = streamingMsgId == msg.id
                            val displayText = if (isThisStreaming && streamingText != null) streamingText else msg.text

                            val cursorInfinite = rememberInfiniteTransition(label = "cursor_${msg.id}")
                            val cursorAlpha by if (isThisStreaming) {
                                cursorInfinite.animateFloat(
                                    initialValue = 1f, targetValue = 0f,
                                    animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
                                    label = "cursorBlink"
                                )
                            } else {
                                cursorInfinite.animateFloat(
                                    initialValue = 0f, targetValue = 0f,
                                    animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Restart),
                                    label = "cursorOff"
                                )
                            }

                            val botBubbleInteraction = remember { MutableInteractionSource() }
                            val isBotBubblePressed by botBubbleInteraction.collectIsPressedAsState()
                            val isReactionActiveOnThis = reactionPickerMessageId == msg.id
                            val botBubbleScale by animateFloatAsState(
                                targetValue = when {
                                    isBotBubblePressed -> 0.985f
                                    isReactionActiveOnThis -> 1.01f
                                    else -> 1f
                                },
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                label = "botBubbleScale_${msg.id}"
                            )

                            var showSecondaryAiActions by remember(msg.id) { mutableStateOf(false) }
                            val aiPillsScrollState = rememberScrollState()

                            LaunchedEffect(showSecondaryAiActions) {
                                if (showSecondaryAiActions) {
                                    kotlinx.coroutines.delay(60)
                                    aiPillsScrollState.animateScrollTo(320)
                                }
                            }

                            // Always use consistent botTop/botMid/botBottom so the sigil ring center is always at exact (71.5 / 841, 50.5 / 841)
                            val bTop = refGlassBitmaps.botTop
                            val bMid = refGlassBitmaps.botMid
                            val bBot = refGlassBitmaps.botBottom

                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                BoxWithConstraints(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            scaleX = botBubbleScale
                                            scaleY = botBubbleScale
                                        }
                                        .aetheriaThreeSliceGlassBubble(
                                            rootSize = rootSize,
                                            blurredBitmap = blurredBgBitmap,
                                            topSlice = bTop,
                                            midSlice = bMid,
                                            bottomSlice = bBot,
                                            isUserBubble = false,
                                            frostedAlpha = 0.62f
                                        )
                                        .combinedClickable(
                                            interactionSource = botBubbleInteraction,
                                            indication = null,
                                            onClick = {
                                                if (reactionPickerMessageId != null) {
                                                    reactionPickerMessageId = null
                                                } else {
                                                    val willOpen = expandedMenuMessageId != msg.id
                                                    expandedMenuMessageId = if (willOpen) msg.id else null
                                                    if (!willOpen) showSecondaryAiActions = false
                                                }
                                            },
                                            onLongClick = {
                                                expandedMenuMessageId = null
                                                expandedUserMenuMessageId = null
                                                showSecondaryAiActions = false
                                                reactionPickerMessageId = if (reactionPickerMessageId == msg.id) null else msg.id
                                            }
                                        )
                                ) {
                                    val bW = maxWidth

                                    // 1:1 Symmetrical Cosmic Sigil Medallion (img_chat_ref_sigil_noletter) + Centered Bot Avatar / "A" Initial
                                    // Placed in a dedicated 1:1 square Box at (x = 18/841, y = 8/841, size = 104/841) so the ring and letter are NEVER squished or misaligned!
                                    Box(
                                        modifier = Modifier
                                            .offset(
                                                x = bW * (18f / 841f),
                                                y = bW * (8f / 841f)
                                            )
                                            .size(bW * (104f / 841f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (refGlassBitmaps.sigilNoLetter.width > 1) {
                                            androidx.compose.foundation.Image(
                                                bitmap = refGlassBitmaps.sigilNoLetter,
                                                contentDescription = null,
                                                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        val fallbackSigilText: @Composable () -> Unit = {
                                            Text(
                                                text = avatarInitial,
                                                fontSize = 19.sp,
                                                fontWeight = FontWeight.Normal,
                                                fontFamily = DisplaySerifFontFamily,
                                                style = TextStyle(
                                                    brush = Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color(0xFFFFFFFF),
                                                            Color(0xFFE9D5FF),
                                                            Color(0xFFC084FC)
                                                        )
                                                    ),
                                                    shadow = Shadow(
                                                        color = Color(0xCCA855F7),
                                                        offset = Offset(0f, 0f),
                                                        blurRadius = 10f
                                                    )
                                                )
                                            )
                                        }
                                        if (bot.avatarUrl.isNotBlank()) {
                                            com.example.ui.components.BotAvatarImage(
                                                avatarUrl = bot.avatarUrl,
                                                contentDescription = headerTitle,
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(bW * (54f / 841f))
                                                    .clip(CircleShape),
                                                fallback = fallbackSigilText
                                            )
                                        } else {
                                            fallbackSigilText()
                                        }
                                    }

                                    // Narrative story text & timestamp inside the translucent bot glass bubble (lower top start so text never touches the top glass border!)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = bW * (180f / 841f))
                                            .padding(
                                                start = bW * (156f / 841f),
                                                end = bW * (84f / 841f),
                                                top = bW * (54f / 841f),
                                                bottom = 16.dp
                                            ),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = formatNarrativeText(displayText + if (isThisStreaming && cursorAlpha > 0.5f) "▌" else ""),
                                            color = Color.White.copy(alpha = 0.97f),
                                            fontSize = 15.sp,
                                            lineHeight = 22.5.sp,
                                            letterSpacing = 0.1.sp,
                                            fontFamily = LiterarySerifFontFamily,
                                            style = TextStyle(
                                                shadow = Shadow(
                                                    color = Color(0x88120524),
                                                    offset = Offset(0f, 1f),
                                                    blurRadius = 3.5f
                                                )
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (msg.reaction.isNotBlank()) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(50))
                                                        .background(Color(0x553B1568))
                                                        .border(0.9.dp, Color(0xBBE9D5FF), RoundedCornerShape(50))
                                                        .clickable(
                                                            interactionSource = remember { MutableInteractionSource() },
                                                            indication = null
                                                        ) {
                                                            viewModel.setMessageReaction(msg.id, "")
                                                        }
                                                        .padding(horizontal = 8.dp, vertical = 2.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(text = msg.reaction, fontSize = 13.sp)
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.width(1.dp))
                                            }

                                            Row(
                                                modifier = Modifier
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = null
                                                    ) {
                                                        if (isSpeaking) onStopSpeaking() else onSpeakText?.invoke(msg.text)
                                                    },
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = timeFormatted.ifBlank { "22:50" },
                                                    color = Color(0xEBF8F5FF),
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Normal,
                                                    fontFamily = InterFontFamily
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                if (isSpeaking) {
                                                    Text(
                                                        text = "🔊",
                                                        color = Color(0xFFE879F9),
                                                        fontSize = 11.5.sp
                                                    )
                                                } else {
                                                    DoubleCheckmarkIcon(tint = Color(0xEBFFFFFF))
                                                }
                                            }
                                        }
                                    }
                                }

                                // ── SECTION 4: MESAJ ALTI AKSİYON KAPSÜLLERİ (Live Mor Cam Glass Pills — Always True Glass Before & After "Diğer"!) ──
                                val showAiPills = expandedMenuMessageId == msg.id && reactionPickerMessageId == null

                                androidx.compose.animation.AnimatedVisibility(
                                    visible = showAiPills,
                                    enter = fadeIn(tween(180)) + expandVertically(tween(180)),
                                    exit = fadeOut(tween(140)) + shrinkVertically(tween(140))
                                ) {
                                    BoxWithConstraints(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        val rowStartPad = maxWidth * (123f / 841f)
                                        val rowEndPad = if (showSecondaryAiActions) 24.dp else maxWidth * (59f / 841f)
                                        val startFraction = (rowStartPad / maxWidth).coerceIn(0.05f, 0.25f)

                                        @Composable
                                        fun BotGlassActionPill(
                                            iconType: String? = null,
                                            label: String,
                                            enabled: Boolean = true,
                                            isDanger: Boolean = false,
                                            isActive: Boolean = false,
                                            pillModifier: Modifier = Modifier,
                                            onClick: () -> Unit
                                        ) {
                                            Row(
                                                modifier = pillModifier
                                                    .height(38.dp)
                                                    .morCamOuterGlow(cornerRadius = 19.dp, shadowAlpha = 0.34f, rimGlowAlpha = 0.24f, bottomFlareAlpha = 0.42f)
                                                    .clip(RoundedCornerShape(50))
                                                    .aetheriaBackdropBlur(rootSize, blurredBgBitmap, alpha = 0.82f)
                                                    .morCamGlassSurface(
                                                        baseSmokeColor = when {
                                                            isDanger -> Color(0x603E0E1A)
                                                            isActive -> Color(0x664C1D95)
                                                            else -> Color(0x52160A2B)
                                                        },
                                                        topRightFrostAlpha = 0.22f,
                                                        topLeftFrostAlpha = 0.18f,
                                                        topStarFrostAlpha = 0f,
                                                        bottomEdgeRefractionAlpha = 0.18f,
                                                        cornerRadius = 19.dp
                                                    )
                                                    .border(
                                                        BorderStroke(
                                                            0.95.dp,
                                                            if (isDanger) {
                                                                Brush.linearGradient(listOf(Color(0xCCF87171), Color(0x99EF4444)))
                                                            } else if (isActive) {
                                                                Brush.linearGradient(listOf(Color(0xEEFAF5FF), Color(0xCCA855F7), Color(0xEEE9D5FF)))
                                                            } else {
                                                                Brush.linearGradient(
                                                                    listOf(Color(0xCCE9D5FF), Color(0x889333EA), Color(0xCCD8B4FE))
                                                                )
                                                            }
                                                        ),
                                                        RoundedCornerShape(50)
                                                    )
                                                    .morCamTopAndBottomSpecular(cornerRadius = 19.dp, topSheenAlpha = 0.24f, bottomFlareAlpha = 0.46f)
                                                    .clickable(enabled = enabled) { onClick() }
                                                    .padding(start = if (iconType != null) 6.dp else 12.dp, end = 11.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)
                                            ) {
                                                if (iconType != null) {
                                                    GlassPillIconBadge(type = iconType, modifier = Modifier.size(23.dp))
                                                }
                                                Text(
                                                    text = label,
                                                    color = if (isDanger) Color(0xFFFCA5A5) else Color(0xFFFBF8FF),
                                                    fontSize = 12.2.sp,
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                    fontFamily = LiterarySerifFontFamily
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .then(
                                                    if (showSecondaryAiActions) {
                                                        Modifier
                                                            .graphicsLayer {
                                                                compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen
                                                            }
                                                            .drawWithContent {
                                                                drawContent()
                                                                drawRect(
                                                                    brush = Brush.horizontalGradient(
                                                                        0.0f to Color.Transparent,
                                                                        (startFraction * 0.72f) to Color.Transparent,
                                                                        (startFraction + 0.04f) to Color.Black,
                                                                        0.94f to Color.Black,
                                                                        1.0f to Color.Transparent
                                                                    ),
                                                                    blendMode = BlendMode.DstIn
                                                                )
                                                            }
                                                            .horizontalScroll(aiPillsScrollState)
                                                    } else {
                                                        Modifier
                                                    }
                                                )
                                                .padding(start = rowStartPad, end = rowEndPad, top = 6.dp, bottom = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            BotGlassActionPill(
                                                iconType = "copy",
                                                label = "Kopyala",
                                                pillModifier = if (!showSecondaryAiActions) Modifier.weight(1.06f) else Modifier
                                            ) {
                                                val cleanText = cleanCopyText(msg.text)
                                                clipboardManager.setText(AnnotatedString(cleanText))
                                                android.widget.Toast.makeText(context, "Kopyalandı", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                            BotGlassActionPill(
                                                iconType = "continue",
                                                label = "Devam Et",
                                                enabled = !isSending,
                                                pillModifier = if (!showSecondaryAiActions) Modifier.weight(1.16f) else Modifier
                                            ) {
                                                expandedMenuMessageId = null
                                                showSecondaryAiActions = false
                                                if (onContinueScene != null) onContinueScene()
                                                else viewModel.continueStoryScene()
                                            }
                                            BotGlassActionPill(
                                                iconType = "more",
                                                label = "Diğer",
                                                isActive = showSecondaryAiActions,
                                                pillModifier = if (!showSecondaryAiActions) Modifier.weight(0.92f) else Modifier
                                            ) {
                                                showSecondaryAiActions = !showSecondaryAiActions
                                            }

                                            androidx.compose.animation.AnimatedVisibility(
                                                visible = showSecondaryAiActions,
                                                enter = fadeIn(tween(180)) + androidx.compose.animation.expandHorizontally(tween(220, easing = FastOutSlowInEasing)),
                                                exit = fadeOut(tween(140)) + androidx.compose.animation.shrinkHorizontally(tween(180))
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    BotGlassActionPill(label = "🔄 Yeniden Oluştur", enabled = !isSending) {
                                                        expandedMenuMessageId = null
                                                        showSecondaryAiActions = false
                                                        onRegenerate()
                                                    }
                                                    BotGlassActionPill(label = if (isSpeaking) "⏹️ Durdur" else "🎙️ Sesli Oku") {
                                                        if (isSpeaking) onStopSpeaking() else onSpeakText?.invoke(msg.text)
                                                    }
                                                    BotGlassActionPill(label = if (msg.isStarred) "⭐ Yıldızı Kaldır" else "☆ Yıldızla") {
                                                        viewModel.toggleMessageStar(msg.id)
                                                    }
                                                    BotGlassActionPill(label = "⏪ Buraya Sar") {
                                                        rewindTargetMessageId = msg.id
                                                        expandedMenuMessageId = null
                                                        showSecondaryAiActions = false
                                                    }
                                                    BotGlassActionPill(label = "✏️ Düzenle") {
                                                        editingMessageId = msg.id
                                                        editingText = msg.text
                                                        expandedMenuMessageId = null
                                                        showSecondaryAiActions = false
                                                    }
                                                    BotGlassActionPill(label = "🗑️ Sil", isDanger = true) {
                                                        expandedMenuMessageId = null
                                                        showSecondaryAiActions = false
                                                        onDeleteMessage(msg.id)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // 😊 Reaction Picker Popup (Translucent Glass Capsule)
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = reactionPickerMessageId == msg.id,
                                    enter = androidx.compose.animation.fadeIn(tween(150)) +
                                        androidx.compose.animation.scaleIn(tween(150), transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 1f)),
                                    exit = androidx.compose.animation.fadeOut(tween(100)) +
                                        androidx.compose.animation.scaleOut(tween(100), transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 1f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(start = 48.dp, top = 6.dp, bottom = 4.dp)
                                            .morCamOuterGlow(cornerRadius = 22.dp, shadowAlpha = 0.34f, rimGlowAlpha = 0.34f, bottomFlareAlpha = 0.68f)
                                            .clip(RoundedCornerShape(22.dp))
                                            .aetheriaBackdropBlur(rootSize, blurredBgBitmap, alpha = 0.72f)
                                            .morCamGlassSurface(
                                                baseSmokeColor = Color(0x28180830),
                                                topRightFrostAlpha = 0.24f,
                                                topLeftFrostAlpha = 0.16f,
                                                topStarFrostAlpha = 0f,
                                                bottomEdgeRefractionAlpha = 0.20f
                                            )
                                            .border(1.dp, Brush.linearGradient(listOf(Color(0xFFE9D5FF), Color(0xFFA855F7), Color(0xFFD8B4FE))), RoundedCornerShape(22.dp))
                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            reactionEmojis.forEach { emoji ->
                                                val isSelected = msg.reaction == emoji
                                                val emojiInteraction = remember { MutableInteractionSource() }
                                                val emojiPressed by emojiInteraction.collectIsPressedAsState()
                                                val emojiScale by animateFloatAsState(
                                                    targetValue = when {
                                                        isSelected -> 1.25f
                                                        emojiPressed -> 0.85f
                                                        else -> 1f
                                                    },
                                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                                    label = "emojiScale_$emoji"
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .graphicsLayer { scaleX = emojiScale; scaleY = emojiScale }
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (isSelected)
                                                                Brush.radialGradient(listOf(Color(0xFF9333EA), Color(0xFF4C1D95)))
                                                            else
                                                                Brush.radialGradient(listOf(Color(0x38A855F7), Color(0x10000000)))
                                                        )
                                                        .clickable(interactionSource = emojiInteraction, indication = null) {
                                                            viewModel.setMessageReaction(msg.id, if (isSelected) "" else emoji)
                                                            reactionPickerMessageId = null
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(emoji, fontSize = 18.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Sleek Aetheria crystal shimmer indicator (shown only before streaming text begins — no 3 bouncing dots, no "hikâyeyi dokuyor" text, no squished sigil!)
                    if (isSending && streamingText.isNullOrBlank()) {
                        item(key = "aetheria_shimmer_indicator") {
                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp, bottom = 6.dp)
                            ) {
                                val tW = maxWidth
                                val shimmerProgress by infiniteTransition.animateFloat(
                                    initialValue = 0f,
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1400, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "aetheriaShimmerBar"
                                )
                                Row(
                                    modifier = Modifier
                                        .padding(start = tW * (123f / 841f))
                                        .height(38.dp)
                                        .morCamOuterGlow(cornerRadius = 19.dp, shadowAlpha = 0.30f, rimGlowAlpha = 0.28f, bottomFlareAlpha = 0.50f)
                                        .clip(RoundedCornerShape(50))
                                        .aetheriaBackdropBlur(rootSize, blurredBgBitmap, alpha = 0.70f)
                                        .morCamGlassSurface(
                                            baseSmokeColor = Color(0x48180832),
                                            topRightFrostAlpha = 0.22f,
                                            topLeftFrostAlpha = 0.16f,
                                            topStarFrostAlpha = 0f,
                                            bottomEdgeRefractionAlpha = 0.20f,
                                            cornerRadius = 19.dp
                                        )
                                        .border(
                                            BorderStroke(
                                                1.dp,
                                                Brush.linearGradient(
                                                    listOf(
                                                        Color(0xFFE9D5FF).copy(alpha = 0.55f + 0.35f * shimmerProgress),
                                                        Color(0xFFA855F7).copy(alpha = 0.45f),
                                                        Color(0xFFD8B4FE).copy(alpha = 0.85f - 0.30f * shimmerProgress)
                                                    )
                                                )
                                            ),
                                            RoundedCornerShape(50)
                                        )
                                        .padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "✦",
                                        color = Color(0xFFE9D5FF),
                                        fontSize = 13.sp,
                                        modifier = Modifier.graphicsLayer {
                                            rotationZ = sweepRotateAngle
                                            scaleX = pulseScale
                                            scaleY = pulseScale
                                        }
                                    )
                                    // Smooth luminous crystal progress bar instead of bouncing 3 dots
                                    Box(
                                        modifier = Modifier
                                            .width(68.dp)
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(Color(0x443B1568))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(0.35f + 0.65f * shimmerProgress)
                                                .clip(RoundedCornerShape(50))
                                                .background(
                                                    Brush.horizontalGradient(
                                                        listOf(Color(0xFF9333EA), Color(0xFFE9D5FF), Color(0xFFC084FC))
                                                    )
                                                )
                                        )
                                    }
                                    Text(
                                        text = "Yanıt hazırlanıyor",
                                        color = Color(0xFFE9D5FF).copy(alpha = 0.88f),
                                        fontSize = 12.sp,
                                        fontFamily = LiterarySerifFontFamily
                                    )
                                }
                            }
                        }
                    }
                }
            }

            val showScrollToBottom = remember {
                derivedStateOf {
                    if (!listState.canScrollForward) return@derivedStateOf false
                    val layoutInfo = listState.layoutInfo
                    val totalItems = layoutInfo.totalItemsCount
                    if (totalItems == 0) return@derivedStateOf false

                    val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf false
                    val itemsFromEnd = totalItems - 1 - lastVisibleItem.index

                    if (itemsFromEnd >= 1) {
                        true
                    } else {
                        val contentEnd = layoutInfo.viewportEndOffset - layoutInfo.afterContentPadding
                        val itemBottom = lastVisibleItem.offset + lastVisibleItem.size
                        (itemBottom - contentEnd) > 350
                    }
                }
            }

            if (showScrollToBottom.value) {
                val totalCount = messages.size + if (isSending) 1 else 0
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = bottomInsetPadding + 10.dp, end = 14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .morCamOuterGlow(cornerRadius = 22.dp, shadowAlpha = 0.32f, rimGlowAlpha = 0.28f, bottomFlareAlpha = 0.56f)
                            .clip(RoundedCornerShape(50))
                            .aetheriaBackdropBlur(rootSize, blurredBgBitmap, alpha = 0.72f)
                            .morCamGlassSurface(
                                baseSmokeColor = Color(0x28180832),
                                topRightFrostAlpha = 0.24f,
                                topLeftFrostAlpha = 0.16f,
                                topStarFrostAlpha = 0f,
                                bottomEdgeRefractionAlpha = 0.20f
                            )
                            .border(
                                BorderStroke(
                                    0.9.dp,
                                    Brush.linearGradient(
                                        listOf(Color(0xE0F3E8FF), Color(0x66A855F7), Color(0xD8E9D5FF))
                                    )
                                ),
                                RoundedCornerShape(50)
                            )
                            .morCamTopAndBottomSpecular(cornerRadius = 22.dp, topSheenAlpha = 0.26f, bottomFlareAlpha = 0.82f)
                            .clickable {
                                coroutineScope.launch {
                                    if (totalCount > 0) {
                                        listState.animateScrollToItem(totalCount - 1)
                                    }
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "En aşağı in",
                                tint = Color(0xFFFBF8FF),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "En Aşağı İn",
                                color = Color(0xFFFBF8FF),
                                fontSize = 11.8.sp,
                                fontFamily = LiterarySerifFontFamily
                            )
                        }
                    }
                }
            }
        }
    }

        if (showBotSettings) {
            BotSettingsModal(
                bot = bot,
                keyCharacters = keyCharacters,
                characterEmotions = characterEmotions,
                affectionEvents = affectionEvents,
                onDismiss = { showBotSettings = false },
                onSave = onSaveBotProfile,
                onResetChat = onResetChat,
                onDeleteBot = onDeleteBot,
                onCloneBot = onCloneBot
            )
        }
    }
}

    if (showBackgroundPicker) {
        MorCamDialogShell(
            onDismissRequest = { showBackgroundPicker = false },
            title = "Sahne Atmosferi & Arka Plan",
            subtitle = "Sohbet ekranında görünmesini istediğiniz atmosferi seçin",
            icon = Icons.Default.Wallpaper
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MorCamOptionRow(
                    title = "Karakter Portresini Arka Plan Yap",
                    subtitle = "Karakterin portresi yumuşak cam katmanla arkaplana yayılır",
                    icon = Icons.Default.Face,
                    selected = bot.chatBgUrl == "USE_AVATAR",
                    onClick = {
                        onSaveBotProfile(bot.copy(chatBgUrl = "USE_AVATAR"), keyCharacters)
                        showBackgroundPicker = false
                    }
                )
                MorCamOptionRow(
                    title = "Aetheria Gotik Katedral Teması",
                    subtitle = "Varsayılan mor salkımlı kozmik cam atmosferi",
                    icon = Icons.Default.AutoAwesome,
                    selected = bot.chatBgUrl.isBlank(),
                    onClick = {
                        onSaveBotProfile(bot.copy(chatBgUrl = ""), keyCharacters)
                        showBackgroundPicker = false
                    }
                )
                MorCamOptionRow(
                    title = "Galeriden Özel Fotoğraf Seç",
                    subtitle = "Bot ayarlarından kendi sahne görselini yükle",
                    icon = Icons.Default.Image,
                    selected = bot.chatBgUrl.isNotBlank() && bot.chatBgUrl != "USE_AVATAR",
                    onClick = {
                        showBackgroundPicker = false
                        showBotSettings = true
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                MorCamCapsuleButton(
                    text = "Kapat",
                    onClick = { showBackgroundPicker = false },
                    variant = MorCamButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // --- 3.2: Pending Cast Member Banner (non-blocking, sohbet akışını kesmez) ---
    val pendingCastMember by viewModel.pendingCastMember.collectAsState()
    LaunchedEffect(bot.id, messages.size) {
        viewModel.checkForPendingCastMembers(bot.id, messages.size)
    }
    AnimatedVisibility(
        visible = pendingCastMember != null && !showBotSettings,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(tween(250)),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(tween(200))
    ) {
        pendingCastMember?.let { pending ->
            MorCamGlassCard(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .morCamCosmicGlow(cornerRadius = 18.dp, alpha = 0.34f, blurDp = 10.dp)
                    .testTag("pending_cast_banner"),
                cornerRadius = 18.dp,
                highlighted = true,
                paddingHorizontal = 14.dp,
                paddingVertical = 12.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Color(0x998B5CF6), Color(0x663B1470))))
                            .border(0.9.dp, MorCamTokens.RimGradient, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MorCamTokens.SilverLavenderBright,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Yeni karakter fark ettim: ${pending.name}",
                        color = MorCamTokens.SilverLavenderBright,
                        fontFamily = LiterarySerifFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = pending.description.take(90).ifBlank { pending.role },
                    color = MorCamTokens.LavenderMuted,
                    fontFamily = InterFontFamily,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MorCamChipPill(
                        label = "Evet, ekle",
                        selected = true,
                        onClick = { viewModel.confirmPendingCastMember(bot.id, pending.id) },
                        modifier = Modifier.testTag("pending_cast_confirm_button")
                    )
                    MorCamChipPill(
                        label = "Şimdilik geç",
                        selected = false,
                        onClick = { viewModel.dismissCastMemberTemporarily(bot.id, pending.id) },
                        modifier = Modifier.testTag("pending_cast_dismiss_button")
                    )
                    MorCamChipPill(
                        label = "Bir daha sorma",
                        selected = false,
                        onClick = { viewModel.blacklistAndDismissCastMember(bot.id, pending.id) },
                        modifier = Modifier.testTag("pending_cast_blacklist_button")
                    )
                }
            }
        }
    }

    if (rewindTargetMessageId != null) {
        val targetId = rewindTargetMessageId!!
        MorCamDialogShell(
            onDismissRequest = { rewindTargetMessageId = null },
            title = "Buradan Devam Et",
            subtitle = "Zaman çizgisini bu mesaja geri sar",
            icon = Icons.Default.History
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Bu noktadan devam edersen sonraki mesajlar ve onlarla ilgili hafıza ile duygu değişimleri silinecek. Emin misin?",
                    color = MorCamTokens.LavenderLight,
                    fontFamily = LiterarySerifFontFamily,
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MorCamCapsuleButton(
                        text = "İptal",
                        onClick = { rewindTargetMessageId = null },
                        variant = MorCamButtonVariant.Secondary,
                        modifier = Modifier.weight(1f)
                    )
                    MorCamCapsuleButton(
                        text = "Evet, Devam Et",
                        onClick = {
                            val idToRewind = targetId
                            rewindTargetMessageId = null
                            onRewindToMessage?.invoke(idToRewind)
                        },
                        variant = MorCamButtonVariant.Destructive,
                        modifier = Modifier.weight(1.25f)
                    )
                }
            }
        }
    }

    if (showBotSettings && isBookMode) {
        BotSettingsModal(
            bot = bot,
            keyCharacters = keyCharacters,
            characterEmotions = characterEmotions,
            affectionEvents = affectionEvents,
            onDismiss = { showBotSettings = false },
            onSave = onSaveBotProfile,
            onResetChat = onResetChat,
            onDeleteBot = onDeleteBot,
            onCloneBot = onCloneBot
        )
    }

    // ✦ Milestone Overlay — 100% Mor Cam Lavender-Silver Celebration
    AnimatedVisibility(
        visible = activeMilestone != null,
        enter = fadeIn(tween(380)) + scaleIn(tween(380, easing = FastOutSlowInEasing), initialScale = 0.78f),
        exit = fadeOut(tween(420)) + scaleOut(tween(280), targetScale = 0.85f)
    ) {
        val milestone = activeMilestone ?: return@AnimatedVisibility
        val infinite = rememberInfiniteTransition(label = "milestoneGlow")
        val glowAlpha by infinite.animateFloat(0.55f, 1f, infiniteRepeatable(tween(950), RepeatMode.Reverse), label = "mg")
        val particleProgress by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(2400, easing = LinearEasing)), label = "mp")

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xB3070214))
                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                        activeMilestone = null
                    }
            )

            // 0 KB Compose Canvas starlight particles — pure lavender/silver/amethyst palette
            Canvas(modifier = Modifier.fillMaxSize()) {
                val starColors = listOf(
                    MorCamTokens.SilverLavenderBright,
                    MorCamTokens.LavenderLight,
                    MorCamTokens.LavenderSoft,
                    MorCamTokens.AmethystGlow,
                    Color(0xFFC4B5FD)
                )
                repeat(28) { i ->
                    val x = size.width * ((i * 0.137f + particleProgress * 0.55f) % 1f)
                    val y = size.height * ((i * 0.173f + particleProgress * 0.35f + i * 0.025f) % 1f)
                    val r = 3.5f + (i % 4) * 2.2f
                    if (i % 4 == 0) {
                        drawMorCamFourPointStar(
                            center = Offset(x, y),
                            outerRadius = r * 2.2f,
                            innerRadius = r * 0.7f,
                            coreColor = Color.White.copy(alpha = 0.85f),
                            edgeColor = starColors[i % starColors.size],
                            glowAlpha = 0.35f
                        )
                    } else {
                        drawCircle(
                            color = starColors[i % starColors.size].copy(alpha = 0.65f),
                            radius = r,
                            center = Offset(x, y)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .morCamCosmicGlow(cornerRadius = 28.dp, alpha = 0.48f, blurDp = 16.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MorCamTokens.GlassSurfaceTop,
                                    MorCamTokens.GlassSurfaceMid,
                                    MorCamTokens.GlassSurfaceBottom
                                )
                            )
                        )
                        .border(BorderStroke(1.3.dp, MorCamTokens.RimGradient), RoundedCornerShape(28.dp))
                        .padding(horizontal = 28.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Color(0xAA9333EA), Color(0x553B1470))))
                            .border(1.2.dp, MorCamTokens.RimGradient, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = milestone.emoji,
                            fontSize = 36.sp,
                            modifier = Modifier.graphicsLayer { alpha = glowAlpha }
                        )
                    }
                    Text(
                        text = "YENİ KİLOMETRE TAŞI",
                        fontFamily = InterFontFamily,
                        color = MorCamTokens.LavenderSoft,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.6.sp
                    )
                    Text(
                        text = milestone.title,
                        fontFamily = DisplaySerifFontFamily,
                        color = MorCamTokens.SilverLavenderBright,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        style = TextStyle(shadow = MorCamTokens.TitleShadow)
                    )
                    MorCamDivider()
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MorCamChipPill(
                            label = "Sevgi ${milestone.affectionAtTime}",
                            selected = false,
                            onClick = {}
                        )
                        MorCamChipPill(
                            label = "Güven ${milestone.trustAtTime}",
                            selected = false,
                            onClick = {}
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    MorCamCapsuleButton(
                        text = "Harika",
                        onClick = { activeMilestone = null },
                        variant = MorCamButtonVariant.Primary,
                        icon = Icons.Default.AutoAwesome,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Canvas(
                    modifier = Modifier
                        .size(26.dp)
                        .align(Alignment.TopCenter)
                ) {
                    drawMorCamFourPointStar(
                        center = Offset(size.width / 2f, size.height / 2f),
                        outerRadius = size.minDimension * 0.48f,
                        innerRadius = size.minDimension * 0.16f
                    )
                }
            }
        }
    }

    // ✦ Starred Messages BottomSheet — 100% Mor Cam Lavender-Silver
    if (showStarredSheet) {
        LaunchedEffect(showStarredSheet) { viewModel.loadStarredMessages() }
        MorCamBottomSheetShell(
            onDismissRequest = { showStarredSheet = false },
            title = "Yıldızlanan Mesajlar",
            subtitle = "${starredMessages.size} mesaj kaydedildi",
            icon = Icons.Default.Star
        ) {
            if (starredMessages.isEmpty()) {
                MorCamGlassCard(
                    cornerRadius = 20.dp,
                    paddingHorizontal = 22.dp,
                    paddingVertical = 28.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(Color(0x888B5CF6), Color(0x443B1470))))
                                .border(1.1.dp, MorCamTokens.RimGradient, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(30.dp)) {
                                drawMorCamFourPointStar(
                                    center = Offset(size.width / 2f, size.height / 2f),
                                    outerRadius = size.minDimension * 0.46f,
                                    innerRadius = size.minDimension * 0.16f
                                )
                            }
                        }
                        Text(
                            text = "Henüz yıldızlanan mesaj yok",
                            fontFamily = LiterarySerifFontFamily,
                            color = MorCamTokens.SilverLavenderBright,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Sohbetteki özel anları saklamak için mesajlara uzun basarak yıldızlayabilirsin.",
                            fontFamily = InterFontFamily,
                            color = MorCamTokens.LavenderMuted,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 460.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(starredMessages, key = { it.id }) { starred ->
                        val isAi = starred.role == "assistant"
                        MorCamGlassCard(
                            cornerRadius = 18.dp,
                            highlighted = !isAi,
                            paddingHorizontal = 14.dp,
                            paddingVertical = 12.dp
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.radialGradient(
                                                        if (isAi) listOf(Color(0xAA7C3AED), Color(0x663B1470))
                                                        else listOf(Color(0xAA9333EA), Color(0x664C1D95))
                                                    )
                                                )
                                                .border(0.9.dp, MorCamTokens.RimGradient, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isAi) Icons.Default.AutoAwesome else Icons.Default.Person,
                                                contentDescription = null,
                                                tint = MorCamTokens.SilverLavenderBright,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Text(
                                            text = if (isAi) bot.aiName else (bot.userCharName.ifBlank { "Sen" }),
                                            fontFamily = LiterarySerifFontFamily,
                                            color = MorCamTokens.LavenderLight,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = MorCamTokens.LavenderSoft,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                MorCamDivider()

                                Text(
                                    text = starred.text.take(220) + if (starred.text.length > 220) "…" else "",
                                    fontFamily = LiterarySerifFontFamily,
                                    color = MorCamTokens.SilverLavenderBright,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    MorCamChipPill(
                                        label = "Yıldızı Kaldır",
                                        selected = false,
                                        icon = Icons.Default.Star,
                                        onClick = { viewModel.toggleMessageStar(starred.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showQuickProfile) {
        val context = LocalContext.current
        BotQuickProfileSheet(
            bot = bot,
            keyCharacters = keyCharacters,
            characterEmotions = characterEmotions,
            affectionEvents = affectionEvents,
            onDismiss = { showQuickProfile = false },
            onSaveBot = { updatedBot ->
                onSaveBotProfile(updatedBot, keyCharacters)
            },
            onOpenFullSettings = { showBotSettings = true },
            onResetChat = { fullReset ->
                showQuickProfile = false
                onResetChat(fullReset)
            },
            onExportChat = {
                val fullText = messages.joinToString("\n\n") { "${if (it.role == "user") bot.userCharName.ifBlank { "Kullanıcı" } else displayName}: ${it.text}" }
                clipboardManager.setText(AnnotatedString(fullText))
                android.widget.Toast.makeText(context, "Tüm sohbet metni panoya kopyalandı!", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }
    } // End of else block for non-book mode
}

data class BookChoiceOption(val text: String, val rawLine: String)

fun parseBookChoiceOptions(rawText: String): List<BookChoiceOption> {
    val result = mutableListOf<BookChoiceOption>()
    val lines = rawText.split("\n")
    var inChoiceSection = false

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.startsWith("🔀")) {
            inChoiceSection = true
            continue
        }

        val isChoice = trimmed.startsWith("1️⃣") || trimmed.startsWith("2️⃣") || trimmed.startsWith("3️⃣") ||
                trimmed.startsWith("4️⃣") || trimmed.startsWith("5️⃣") || trimmed.startsWith("6️⃣") ||
                trimmed.matches(Regex("^[1-9][0-9]*[\\.\\)]\\s*.*")) ||
                (inChoiceSection && (trimmed.startsWith("-") || trimmed.startsWith("•") || trimmed.startsWith("*")))

        if (isChoice) {
            val choiceText = trimmed
                .replace(Regex("^(1️⃣|2️⃣|3️⃣|4️⃣|5️⃣|6️⃣|\\d+[\\.\\)]|[-•*])\\s*"), "")
                .trim()
            if (choiceText.length >= 2) {
                result.add(BookChoiceOption(choiceText, trimmed))
            }
        }
    }
    return result
}

enum class SummaryViewMode {
    PROMPT,
    SEQUENTIAL,
    FINAL_GRID
}

enum class StepPhase {
    CARD_ONLY,
    RESULT_REVEALED
}

data class ChapterChoiceItem(
    val id: Int,
    val title: String,
    val selectedOption: String,
    val impactText: String,
    val icon: String, // "🔀", "💫", "💭"
    val isBranching: Boolean = false,
    val isRelationship: Boolean = false,
    val impactLevel: String = "orta" // "dusuk", "orta", "kritik"
)

@OptIn(ExperimentalMaterial3Api::class)

private data class CosmicParticle(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val baseAlpha: Float,
    val speed: Float
)







fun cleanCopyText(rawText: String): String {
    return com.example.data.repository.EmochiRepository.cleanEmotionTags(rawText)
}