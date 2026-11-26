package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.LiterarySerifFontFamily

private val NARRATIVE_FORMAT_REGEX = Regex("(\"[^\"]*\")|(“[^”]*”)|(«[^»]*»)|(\\*[^\\*]*\\*)")

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
        .replace(
            "\"İstediğimiz veri çipi elimizde ama izimizdeler. Planı devreye sokmaya hazır mısın?\"",
            "“İstediğimiz veri çipi elimizde ama izimizdeler. Planı devreye sokmaya hazır mısın?”"
        )
    return buildAnnotatedString {
        var lastIdx = 0
        val matches = NARRATIVE_FORMAT_REGEX.findAll(clean)

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

// Generates a detail-preserving, natural-contrast optical frosted bitmap from the gothic wisteria background
// Backed by centralized FrostedBackdropCache (LruCache max 4, evicted on onTrimMemory).
fun createFrostedWisteriaBackdropBitmap(context: android.content.Context): ImageBitmap {
    return com.example.util.FrostedBackdropCache.getOrGenerateSync(
        context = context,
        key = "wisteria_chat_bg",
        drawableResId = com.example.R.drawable.img_chat_bg_aiden_wisteria,
        targetWidth = 360,
        maxHeight = 1080,
        blurRadius = 5,
        blurPasses = 2,
        violetShiftWarmPixels = false,
        outputRgb565 = true
    )
}

val LocalAetheriaRootPos = compositionLocalOf { mutableStateOf(Offset.Zero) }
val LocalAetheriaRootSize = compositionLocalOf { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }

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

internal fun smoothRamp(value: Float, edge0: Float, edge1: Float): Float {
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
                x <= 128 -> 1.0f
                x in 129..145 -> 1.0f - smoothRamp(x.toFloat(), 128f, 145f)
                x in 146..756 -> 0.0f
                x in 757..776 -> smoothRamp(x.toFloat(), 757f, 776f)
                else -> 1.0f
            }
        }

        for (y in 0 until h) {
            val fy = when (role) {
                "top" -> when {
                    y <= 32 -> 1.0f
                    y in 33..50 -> 1.0f - smoothRamp(y.toFloat(), 32f, 50f)
                    else -> 0.0f
                }
                "bottom" -> when {
                    y <= 34 -> 0.0f
                    y in 35..50 -> smoothRamp(y.toFloat(), 34f, 50f)
                    else -> 1.0f
                }
                else -> 0.0f
            }
            val rowOffset = y * w
            for (x in 0 until w) {
                if (x < 120) {
                    pixels[rowOffset + x] = 0
                    continue
                }
                val px = pixels[rowOffset + x]
                val a = (px ushr 24) and 0xFF
                if (a > 0) {
                    var edge = maxOf(fxTable[x], fy)
                    if (role == "top" && x in 665..780 && y in 28..82) {
                        val cxFactor = smoothRamp(x.toFloat(), 665f, 755f)
                        val cyFactor = 1.0f - smoothRamp(y.toFloat(), 32f, 82f)
                        edge = maxOf(edge, cxFactor * cyFactor * 0.78f)
                    }
                    val mult = interiorAlpha + (1.0f - interiorAlpha) * edge
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

                val isLeftCapZone = x < cxLeft
                val isRightCapZone = x > cxRight
                val capDist = when {
                    isLeftCapZone -> kotlin.math.hypot((x - cxLeft).toDouble(), (y - cyCap).toDouble()).toFloat()
                    isRightCapZone -> kotlin.math.hypot((x - cxRight).toDouble(), (y - cyCap).toDouble()).toFloat()
                    else -> 0f
                }

                if ((isLeftCapZone || isRightCapZone) && capDist > pillR) {
                    if (capDist < outerGlowMaxR) {
                        val radialFade = 1.0f - smoothRamp(capDist, pillR - 0.5f, outerGlowMaxR)
                        val glowAlpha = (radialFade * radialFade * 155f).toInt().coerceIn(0, 255)
                        if (glowAlpha > 0) {
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

/**
 * Process-scoped cache for the 14 processed ARGB_8888 Aetheria glass reference plates.
 *
 * Eliminates repeated synchronous main-thread PNG decoding + pixel loops whenever ChatScreen
 * or BotSettingsModal opens, prevents duplicate 14-bitmap allocations in RAM, and evicts
 * cleanly from [com.example.EmochiApplication.onTrimMemory].
 */
object AetheriaRefGlassCache {
    private val lock = Any()

    @Volatile
    private var cached: AetheriaRefGlassBitmaps? = null

    val placeholder: AetheriaRefGlassBitmaps by lazy {
        val px = android.graphics.Bitmap.createBitmap(1, 1, android.graphics.Bitmap.Config.ARGB_8888).asImageBitmap()
        AetheriaRefGlassBitmaps(
            headerNoLetter = px,
            sigilNoLetter = px,
            userTop = px,
            userMid = px,
            userBottom = px,
            botTop = px,
            botMid = px,
            botBottom = px,
            bot3Top = px,
            bot3Mid = px,
            bot3Bottom = px,
            pillsRowAligned = px,
            quickCards = px,
            inputBarNoText = px
        )
    }

    fun get(): AetheriaRefGlassBitmaps? = cached

    fun isCached(): Boolean = cached != null

    fun put(bitmaps: AetheriaRefGlassBitmaps) {
        synchronized(lock) {
            cached = bitmaps
        }
    }

    fun evictAll() {
        synchronized(lock) {
            cached = null
        }
    }

    fun getOrLoadSync(context: android.content.Context): AetheriaRefGlassBitmaps {
        cached?.let { return it }
        val appContext = context.applicationContext ?: context
        synchronized(lock) {
            cached?.let { return it }
            val loaded = loadAetheriaRefGlassBitmapsInternal(appContext)
            cached = loaded
            return loaded
        }
    }

    suspend fun getOrLoadAsync(context: android.content.Context): AetheriaRefGlassBitmaps {
        cached?.let { return it }
        val appContext = context.applicationContext ?: context
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            getOrLoadSync(appContext)
        }
    }
}

private fun loadAetheriaRefGlassBitmapsInternal(context: android.content.Context): AetheriaRefGlassBitmaps {
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

fun loadAetheriaRefGlassBitmaps(context: android.content.Context): AetheriaRefGlassBitmaps {
    return AetheriaRefGlassCache.getOrLoadSync(context)
}

@Composable
fun rememberAetheriaRefGlassBitmaps(): AetheriaRefGlassBitmaps {
    val context = androidx.compose.ui.platform.LocalContext.current
    val state = androidx.compose.runtime.produceState(
        initialValue = AetheriaRefGlassCache.get() ?: AetheriaRefGlassCache.placeholder,
        key1 = Unit
    ) {
        if (!AetheriaRefGlassCache.isCached()) {
            value = AetheriaRefGlassCache.getOrLoadAsync(context)
        }
    }
    return state.value
}

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
        .drawWithCache {
            val w = size.width
            val h = size.height
            val validSlices = w > 4f && h > 4f && topSlice.width > 1 && midSlice.width > 1 && bottomSlice.width > 1
            if (!validSlices) {
                onDrawBehind {}
            } else {
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
                val topDstSize = androidx.compose.ui.unit.IntSize(intW, topIntH)
                val midDstOffset = androidx.compose.ui.unit.IntOffset(0, topIntH)
                val midDstSize = androidx.compose.ui.unit.IntSize(intW, midIntH)
                val botDstOffset = androidx.compose.ui.unit.IntOffset(0, topIntH + midIntH)
                val botDstSize = androidx.compose.ui.unit.IntSize(intW, botIntH)

                onDrawBehind {
                    val imgW = blurredBitmap.width
                    val imgH = blurredBitmap.height
                    if (imgW > 1 && imgH > 1) {
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

                    drawImage(
                        image = topSlice,
                        dstOffset = androidx.compose.ui.unit.IntOffset(0, 0),
                        dstSize = topDstSize,
                        filterQuality = FilterQuality.High
                    )
                    if (midIntH > 0) {
                        drawImage(
                            image = midSlice,
                            dstOffset = midDstOffset,
                            dstSize = midDstSize,
                            filterQuality = FilterQuality.High
                        )
                    }
                    drawImage(
                        image = bottomSlice,
                        dstOffset = botDstOffset,
                        dstSize = botDstSize,
                        filterQuality = FilterQuality.High
                    )
                }
            }
        }
}

fun Modifier.morCamGlassSurface(
    baseSmokeColor: Color = Color(0x58160B2B),
    topRightFrostAlpha: Float = 0.28f,
    topLeftFrostAlpha: Float = 0.20f,
    topStarFrostAlpha: Float = 0.22f,
    bottomEdgeRefractionAlpha: Float = 0.18f,
    cornerRadius: androidx.compose.ui.unit.Dp = 22.dp
): Modifier = this.drawWithCache {
    val w = size.width
    val h = size.height
    val cr = cornerRadius.toPx()

    val innerPurplePaint = if (w > 2f && h > 2f) {
        android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 3.0.dp.toPx()
            color = android.graphics.Color.argb(52, 148, 98, 208)
            maskFilter = android.graphics.BlurMaskFilter(5.5.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
    } else null

    val hasTopRight = topRightFrostAlpha > 0f && w > 1f && h > 1f
    val trEdgeBrush = if (hasTopRight) {
        Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                Color(0xFF6F4888).copy(alpha = topRightFrostAlpha * 0.45f),
                Color(0xFF8C5CA2).copy(alpha = topRightFrostAlpha * 0.88f)
            ),
            startX = w * 0.78f,
            endX = w
        )
    } else null
    val trCenter = Offset(w * 0.88f, h * 0.12f)
    val trRadius = maxOf(w * 0.34f, h * 0.44f).coerceAtLeast(1f)
    val trGlowBrush = if (hasTopRight) {
        Brush.radialGradient(
            colors = listOf(
                Color(0xFF9E6BB8).copy(alpha = topRightFrostAlpha * 0.85f),
                Color(0xFF754794).copy(alpha = topRightFrostAlpha * 0.40f),
                Color.Transparent
            ),
            center = trCenter,
            radius = trRadius
        )
    } else null
    val trSpecks = if (hasTopRight && w > 160.dp.toPx() && h > 70.dp.toPx()) {
        listOf(
            Offset(w * 0.89f, h * 0.06f) to 0.9.dp.toPx(),
            Offset(w * 0.93f, h * 0.09f) to 1.1.dp.toPx(),
            Offset(w * 0.95f, h * 0.14f) to 0.8.dp.toPx(),
            Offset(w * 0.91f, h * 0.13f) to 0.7.dp.toPx(),
            Offset(w * 0.96f, h * 0.20f) to 0.85.dp.toPx(),
            Offset(w * 0.86f, h * 0.08f) to 0.75.dp.toPx()
        )
    } else null
    val speckColor = Color(0xFFD8B4FE).copy(alpha = 0.42f)

    val tlCenter = Offset(w * 0.12f, h * 0.12f)
    val tlRadius = maxOf(w * 0.34f, h * 0.42f).coerceAtLeast(1f)
    val tlBrush = if (topLeftFrostAlpha > 0f && w > 1f && h > 1f) {
        Brush.radialGradient(
            colors = listOf(
                Color(0xFF9362D6).copy(alpha = topLeftFrostAlpha * 0.90f),
                Color(0xFF6B3FA8).copy(alpha = topLeftFrostAlpha * 0.42f),
                Color.Transparent
            ),
            center = tlCenter,
            radius = tlRadius
        )
    } else null

    val tcCenter = Offset(w * 0.50f, 0f)
    val tcRadius = (w * 0.20f).coerceAtLeast(1f)
    val tcBrush = if (topStarFrostAlpha > 0f && w > 1f && h > 1f) {
        Brush.radialGradient(
            colors = listOf(
                Color(0xFFC89BFF).copy(alpha = topStarFrostAlpha * 0.85f),
                Color(0xFF8B5CF6).copy(alpha = topStarFrostAlpha * 0.35f),
                Color.Transparent
            ),
            center = tcCenter,
            radius = tcRadius
        )
    } else null

    val blCenter = Offset(w * 0.24f, h)
    val blRadius = maxOf(w * 0.28f, h * 0.28f).coerceAtLeast(1f)
    val blBrush = if (bottomEdgeRefractionAlpha > 0f && h > 1f) {
        Brush.radialGradient(
            colors = listOf(
                Color(0xFF9B6AD6).copy(alpha = bottomEdgeRefractionAlpha * 0.75f),
                Color(0xFF6D3FA6).copy(alpha = bottomEdgeRefractionAlpha * 0.30f),
                Color.Transparent
            ),
            center = blCenter,
            radius = blRadius
        )
    } else null

    onDrawBehind {
        drawRect(color = baseSmokeColor)

        if (innerPurplePaint != null) {
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawRoundRect(0f, 0f, w, h, cr, cr, innerPurplePaint)
            }
        }

        if (trEdgeBrush != null && trGlowBrush != null) {
            drawRect(brush = trEdgeBrush)
            drawCircle(brush = trGlowBrush, radius = trRadius, center = trCenter)
            trSpecks?.forEach { (center, radPx) ->
                drawCircle(color = speckColor, radius = radPx, center = center)
            }
        }

        if (tlBrush != null) {
            drawCircle(brush = tlBrush, radius = tlRadius, center = tlCenter)
        }

        if (tcBrush != null) {
            drawCircle(brush = tcBrush, radius = tcRadius, center = tcCenter)
        }

        if (blBrush != null) {
            drawCircle(brush = blBrush, radius = blRadius, center = blCenter)
        }
    }
}

fun Modifier.morCamOuterGlow(
    cornerRadius: androidx.compose.ui.unit.Dp = 24.dp,
    shadowAlpha: Float = 0.42f,
    rimGlowAlpha: Float = 0.26f,
    bottomFlareAlpha: Float = 0.45f,
    flareColor: Color = Color(0xFF9D6CE0)
): Modifier = this.drawWithCache {
    val cr = cornerRadius.toPx()
    val w = size.width
    val h = size.height
    val cushionPaint = if (shadowAlpha > 0f) {
        android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 5.5.dp.toPx()
            color = android.graphics.Color.argb((shadowAlpha * 235).toInt().coerceIn(0, 255), 22, 9, 42)
            maskFilter = android.graphics.BlurMaskFilter(5.5.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
    } else null
    val rimPaint = if (rimGlowAlpha > 0f) {
        android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 2.0.dp.toPx()
            color = android.graphics.Color.argb((rimGlowAlpha * 220).toInt().coerceIn(0, 255), 147, 88, 232)
            maskFilter = android.graphics.BlurMaskFilter(4.5.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
    } else null
    val flarePaint = if (bottomFlareAlpha > 0f) {
        android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 2.2.dp.toPx()
            color = android.graphics.Color.argb(
                (bottomFlareAlpha * 145).toInt().coerceIn(0, 255),
                (flareColor.red * 255).toInt().coerceIn(0, 255),
                (flareColor.green * 255).toInt().coerceIn(0, 255),
                (flareColor.blue * 255).toInt().coerceIn(0, 255)
            )
            maskFilter = android.graphics.BlurMaskFilter(4.0.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
    } else null

    onDrawBehind {
        drawIntoCanvas { canvas ->
            val native = canvas.nativeCanvas
            if (cushionPaint != null) {
                native.drawRoundRect(0f, 0f, w, h, cr, cr, cushionPaint)
            }
            if (rimPaint != null) {
                native.drawRoundRect(0f, 0f, w, h, cr, cr, rimPaint)
            }
            if (flarePaint != null) {
                native.drawLine(w * 0.16f, h, w * 0.84f, h, flarePaint)
            }
        }
    }
}

fun Modifier.morCamTopAndBottomSpecular(
    cornerRadius: androidx.compose.ui.unit.Dp = 24.dp,
    topSheenAlpha: Float = 0.24f,
    bottomFlareAlpha: Float = 0.78f,
    flareColor: Color = Color(0xFFB985F5)
): Modifier = this.drawWithCache {
    val w = size.width
    val h = size.height
    val inset = cornerRadius.toPx().coerceAtMost(w * 0.25f)
    val topY = 0.65.dp.toPx()
    val botY = h - 0.65.dp.toPx()
    val topStroke = 0.9.dp.toPx()
    val botStroke = 1.0.dp.toPx()

    val topSx = inset * 0.75f
    val topEx = w * 0.46f
    val topBrush = if (topSheenAlpha > 0f && w > inset * 2f && topEx > topSx) {
        Brush.horizontalGradient(
            listOf(
                Color.Transparent,
                Color(0xFFB380F5).copy(alpha = topSheenAlpha * 0.75f),
                Color(0xFFD8B4FE).copy(alpha = topSheenAlpha * 0.95f),
                Color(0xFFA86EE8).copy(alpha = topSheenAlpha * 0.65f),
                Color.Transparent
            ),
            startX = topSx,
            endX = topEx
        )
    } else null

    val botSx = inset * 0.70f
    val botEx = w * 0.58f
    val botBrush = if (bottomFlareAlpha > 0f && w > inset * 2f && botEx > botSx) {
        Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                flareColor.copy(alpha = bottomFlareAlpha * 0.35f),
                Color(0xFFD6B4FC).copy(alpha = bottomFlareAlpha * 0.55f),
                flareColor.copy(alpha = bottomFlareAlpha * 0.30f),
                Color.Transparent
            ),
            startX = botSx,
            endX = botEx
        )
    } else null

    onDrawWithContent {
        drawContent()
        if (topBrush != null) {
            drawLine(
                brush = topBrush,
                start = Offset(topSx, topY),
                end = Offset(topEx, topY),
                strokeWidth = topStroke,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
        if (botBrush != null) {
            drawLine(
                brush = botBrush,
                start = Offset(botSx, botY),
                end = Offset(botEx, botY),
                strokeWidth = botStroke,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithCache {
                        val w = size.width
                        val h = size.height
                        val brCenter = Offset(w * 0.76f, h * 0.78f)
                        val brRadius = (w * 0.42f).coerceAtLeast(1f)
                        val brBrush = Brush.radialGradient(
                            colors = listOf(Color(0xBBE879F9), Color(0x55A855F7), Color.Transparent),
                            center = brCenter,
                            radius = brRadius
                        )
                        val tlCenter = Offset(w * 0.24f, h * 0.22f)
                        val tlRadius = (w * 0.40f).coerceAtLeast(1f)
                        val tlBrush = Brush.radialGradient(
                            colors = listOf(Color(0xAA9333EA), Color(0x447C3AED), Color.Transparent),
                            center = tlCenter,
                            radius = tlRadius
                        )
                        val trCenter = Offset(w * 0.78f, h * 0.26f)
                        val trRadius = (w * 0.30f).coerceAtLeast(1f)
                        val trBrush = Brush.radialGradient(
                            colors = listOf(Color(0x66C084FC), Color.Transparent),
                            center = trCenter,
                            radius = trRadius
                        )
                        val starR = w * 0.068f
                        val starW = w * 0.018f
                        val miniStars = listOf(
                            Offset(w * 0.16f, h * 0.50f),
                            Offset(w * 0.84f, h * 0.50f)
                        ).map { c ->
                            Path().apply {
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
                        }
                        val specks = listOf(
                            Offset(w * 0.24f, h * 0.30f) to 1.1.dp.toPx(),
                            Offset(w * 0.76f, h * 0.25f) to 1.0.dp.toPx(),
                            Offset(w * 0.22f, h * 0.70f) to 0.9.dp.toPx(),
                            Offset(w * 0.75f, h * 0.75f) to 1.4.dp.toPx(),
                            Offset(w * 0.50f, h * 0.17f) to 0.9.dp.toPx(),
                            Offset(w * 0.50f, h * 0.84f) to 1.0.dp.toPx()
                        )
                        val speckColor = Color(0xFFF3E8FF).copy(alpha = 0.88f)
                        onDrawBehind {
                            drawCircle(brush = brBrush, radius = brRadius, center = brCenter)
                            drawCircle(brush = tlBrush, radius = tlRadius, center = tlCenter)
                            drawCircle(brush = trBrush, radius = trRadius, center = trCenter)
                            miniStars.forEach { drawPath(it, Color(0xFFE9D5FF)) }
                            specks.forEach { (pt, radPx) ->
                                drawCircle(color = speckColor, radius = radPx, center = pt)
                            }
                        }
                    }
            )

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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val ringR = size.minDimension * ringRFraction
                    val outerTipCard = size.minDimension * 0.495f
                    val outerTipDiag = size.minDimension * 0.456f

                    val auraPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        style = android.graphics.Paint.Style.STROKE
                        strokeWidth = 3.0.dp.toPx()
                        color = android.graphics.Color.argb(185, 192, 132, 252)
                        maskFilter = android.graphics.BlurMaskFilter(5.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
                    }
                    val outerStroke = Stroke(width = 0.7.dp.toPx())
                    val ringStroke = Stroke(width = 1.25.dp.toPx())
                    val ringBrush = Brush.sweepGradient(
                        listOf(
                            Color(0xFFF3E8FF),
                            Color(0xFFC084FC),
                            Color(0xFFFFFFFF),
                            Color(0xFFA855F7),
                            Color(0xFFF3E8FF)
                        )
                    )
                    val flares = listOf(-42.0, 52.0, -135.0).mapIndexed { idx, deg ->
                        val rad = Math.toRadians(deg)
                        val fx = cx + (Math.cos(rad) * ringR).toFloat()
                        val fy = cy + (Math.sin(rad) * ringR).toFloat()
                        val fR = (if (idx < 2) size.minDimension * 0.14f else size.minDimension * 0.10f).coerceAtLeast(1f)
                        val center = Offset(fx, fy)
                        val brush = Brush.radialGradient(
                            colors = listOf(Color(0xEEFFFFFF), Color(0xAAE879F9), Color(0x44A855F7), Color.Transparent),
                            center = center,
                            radius = fR
                        )
                        Triple(center, fR, brush)
                    }
                    val cardinalSpikePath = Path().apply {
                        val baseHalfW = size.minDimension * 0.028f
                        moveTo(cx, cy - outerTipCard)
                        lineTo(cx + baseHalfW, cy - ringR * 0.96f)
                        lineTo(cx, cy - ringR * 0.89f)
                        lineTo(cx - baseHalfW, cy - ringR * 0.96f)
                        close()
                    }
                    val cardinalBrush = Brush.verticalGradient(
                        colors = listOf(Color.White, Color(0xFFE9D5FF), Color(0xFFA855F7)),
                        startY = cy - outerTipCard,
                        endY = cy - ringR * 0.89f
                    )
                    val diagSpikePath = Path().apply {
                        val baseHalfW = size.minDimension * 0.018f
                        moveTo(cx, cy - outerTipDiag)
                        lineTo(cx + baseHalfW, cy - ringR * 0.96f)
                        lineTo(cx, cy - ringR * 0.89f)
                        lineTo(cx - baseHalfW, cy - ringR * 0.96f)
                        close()
                    }
                    val diagBrush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFE9D5FF), Color(0xFFC084FC), Color(0xFF7C3AED)),
                        startY = cy - outerTipDiag,
                        endY = cy - ringR * 0.89f
                    )
                    val centerOffset = Offset(cx, cy)

                    onDrawBehind {
                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawCircle(cx, cy, ringR, auraPaint)
                        }
                        drawCircle(
                            color = Color(0x709333EA),
                            radius = ringR * 1.09f,
                            center = centerOffset,
                            style = outerStroke
                        )
                        drawCircle(
                            brush = ringBrush,
                            radius = ringR,
                            center = centerOffset,
                            style = ringStroke
                        )
                        flares.forEach { (center, fR, brush) ->
                            drawCircle(brush = brush, radius = fR, center = center)
                        }
                        for (i in 0 until 8) {
                            val isCardinal = i % 2 == 0
                            rotate(degrees = i * 45f, pivot = centerOffset) {
                                drawPath(
                                    path = if (isCardinal) cardinalSpikePath else diagSpikePath,
                                    brush = if (isCardinal) cardinalBrush else diagBrush
                                )
                            }
                        }
                    }
                }
        )
    }
}

@Composable
fun PurpleVerifiedBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(16.dp)
            .drawWithCache {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val outerR = size.minDimension * 0.46f
                val innerR = size.minDimension * 0.36f

                val glowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.argb(175, 168, 85, 247)
                    maskFilter = android.graphics.BlurMaskFilter(3.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
                }
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
                val badgeBrush = Brush.linearGradient(
                    listOf(Color(0xFFC084FC), Color(0xFF9333EA), Color(0xFF6D28D9))
                )
                val badgeStroke = Stroke(width = 0.7.dp.toPx(), join = androidx.compose.ui.graphics.StrokeJoin.Round)
                val checkPath = Path().apply {
                    moveTo(cx - outerR * 0.36f, cy + outerR * 0.02f)
                    lineTo(cx - outerR * 0.08f, cy + outerR * 0.30f)
                    lineTo(cx + outerR * 0.40f, cy - outerR * 0.24f)
                }
                val checkStroke = Stroke(
                    width = 1.55.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
                onDrawBehind {
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawCircle(cx, cy, outerR, glowPaint)
                    }
                    drawPath(path = badgePath, brush = badgeBrush)
                    drawPath(path = badgePath, color = Color(0xCCEDE9FE), style = badgeStroke)
                    drawPath(path = checkPath, color = Color.White, style = checkStroke)
                }
            }
    )
}

@Composable
fun TopBorderGlowingStar(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 104.dp, height = 28.dp)
            .drawWithCache {
                val cx = size.width / 2f
                val cy = size.height / 2f

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
                val lineBrush = Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        Color(0x70935CE0),
                        Color(0xCCBA88F7),
                        Color(0xFFE6CCFF),
                        Color(0xCCBA88F7),
                        Color(0x70935CE0),
                        Color.Transparent
                    )
                )
                val lineStrokeW = 1.15.dp.toPx()
                val glowRadius = 9.dp.toPx()
                val glowBrush = Brush.radialGradient(
                    listOf(Color(0x88C084FC), Color(0x449333EA), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = glowRadius
                )
                val diagLen = 4.2.dp.toPx()
                val diagStrokeW = 0.8.dp.toPx()

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

                onDrawBehind {
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawPath(peakPath, peakPaint)
                    }
                    drawLine(
                        brush = lineBrush,
                        start = Offset(0f, cy),
                        end = Offset(size.width, cy),
                        strokeWidth = lineStrokeW,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    drawCircle(brush = glowBrush, radius = glowRadius, center = Offset(cx, cy))
                    drawLine(
                        color = Color(0x99C084FC),
                        start = Offset(cx - diagLen, cy - diagLen),
                        end = Offset(cx + diagLen, cy + diagLen),
                        strokeWidth = diagStrokeW,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0x99C084FC),
                        start = Offset(cx - diagLen, cy + diagLen),
                        end = Offset(cx + diagLen, cy - diagLen),
                        strokeWidth = diagStrokeW,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    drawPath(path = outerStar, color = Color(0xFFC492FF))
                    drawPath(path = innerStar, color = Color(0xFFF3E4FF))
                }
            }
    )
}

@Composable
fun SingleCheckmarkIcon(
    tint: Color = Color(0xEBFFFFFF),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = 12.dp, height = 11.dp)
            .drawWithCache {
                val w = size.width
                val h = size.height
                val stroke = Stroke(
                    width = 1.45.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
                val tick = Path().apply {
                    moveTo(w * 0.15f, h * 0.56f)
                    lineTo(w * 0.42f, h * 0.82f)
                    lineTo(w * 0.88f, h * 0.18f)
                }
                onDrawBehind {
                    drawPath(path = tick, color = tint, style = stroke)
                }
            }
    )
}

@Composable
fun DoubleCheckmarkIcon(
    tint: Color = Color(0xEBFFFFFF),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = 16.dp, height = 11.dp)
            .drawWithCache {
                val w = size.width
                val h = size.height
                val stroke = Stroke(
                    width = 1.45.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
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
                onDrawBehind {
                    drawPath(path = tick1, color = tint, style = stroke)
                    drawPath(path = tick2, color = tint, style = stroke)
                }
            }
    )
}

@Composable
fun GlassPillIconBadge(type: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(26.dp)
            .drawWithCache {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val r = size.minDimension / 2f
                val ringRadius = r - 0.5.dp.toPx()
                val ringStroke = Stroke(width = 0.75.dp.toPx())
                val center = Offset(cx, cy)

                when (type) {
                    "copy" -> {
                        val bw = size.width * 0.42f
                        val bh = size.height * 0.48f
                        val left = cx - bw / 2f
                        val top = cy - bh / 2f + 1.dp.toPx()
                        val crMain = androidx.compose.ui.geometry.CornerRadius(2.2.dp.toPx(), 2.2.dp.toPx())
                        val cw = bw * 0.54f
                        val ch = 2.6.dp.toPx()
                        val crCut = androidx.compose.ui.geometry.CornerRadius(1.2.dp.toPx(), 1.2.dp.toPx())
                        val crClip = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx(), 1.dp.toPx())
                        val cutOffset = Offset(cx - cw / 2f, top - 0.6.dp.toPx())
                        val cutSize = androidx.compose.ui.geometry.Size(cw, ch + 1.2.dp.toPx())
                        val clipOffset = Offset(cx - cw * 0.38f, top - 1.2.dp.toPx())
                        val clipSize = androidx.compose.ui.geometry.Size(cw * 0.76f, ch)
                        val lineW = 1.15.dp.toPx()
                        onDrawBehind {
                            drawCircle(color = Color(0x881A0B34), radius = r, center = center)
                            drawCircle(color = Color(0x38D8B4FE), radius = ringRadius, center = center, style = ringStroke)
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(left, top),
                                size = androidx.compose.ui.geometry.Size(bw, bh),
                                cornerRadius = crMain
                            )
                            drawRoundRect(color = Color(0xFF1A0B34), topLeft = cutOffset, size = cutSize, cornerRadius = crCut)
                            drawRoundRect(color = Color.White, topLeft = clipOffset, size = clipSize, cornerRadius = crClip)
                            for (i in 0..2) {
                                val ly = top + bh * (0.42f + i * 0.20f)
                                drawLine(
                                    color = Color(0xFF28134A),
                                    start = Offset(left + bw * 0.22f, ly),
                                    end = Offset(left + bw * 0.78f, ly),
                                    strokeWidth = lineW,
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                            }
                        }
                    }
                    "continue" -> {
                        val sq = size.minDimension * 0.64f
                        val left = cx - sq / 2f
                        val top = cy - sq / 2f
                        val cornerPx = 4.dp.toPx()
                        val glowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                            color = android.graphics.Color.argb(175, 245, 158, 11)
                            maskFilter = android.graphics.BlurMaskFilter(4.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
                        }
                        val sqBrush = Brush.linearGradient(
                            colors = listOf(Color(0xFFFBBF24), Color(0xFFF97316)),
                            start = Offset(left, top),
                            end = Offset(left + sq, top + sq)
                        )
                        val sqCorner = androidx.compose.ui.geometry.CornerRadius(cornerPx, cornerPx)
                        val chevStroke = Stroke(
                            width = 1.5.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                            join = androidx.compose.ui.graphics.StrokeJoin.Round
                        )
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
                        onDrawBehind {
                            drawCircle(color = Color(0x881A0B34), radius = r, center = center)
                            drawCircle(color = Color(0x38D8B4FE), radius = ringRadius, center = center, style = ringStroke)
                            drawIntoCanvas { canvas ->
                                canvas.nativeCanvas.drawRoundRect(left, top, left + sq, top + sq, cornerPx, cornerPx, glowPaint)
                            }
                            drawRoundRect(
                                brush = sqBrush,
                                topLeft = Offset(left, top),
                                size = androidx.compose.ui.geometry.Size(sq, sq),
                                cornerRadius = sqCorner
                            )
                            drawPath(leftChev, Color.White, style = chevStroke)
                            drawPath(rightChev, Color.White, style = chevStroke)
                        }
                    }
                    "more" -> {
                        val dotR = 1.75.dp.toPx()
                        val spacing = 4.6.dp.toPx()
                        onDrawBehind {
                            drawCircle(color = Color(0x881A0B34), radius = r, center = center)
                            drawCircle(color = Color(0x38D8B4FE), radius = ringRadius, center = center, style = ringStroke)
                            for (i in -1..1) {
                                drawCircle(color = Color.White, radius = dotR, center = Offset(cx + i * spacing, cy))
                            }
                        }
                    }
                    else -> onDrawBehind {}
                }
            }
    )
}

@Composable
fun QuickActionIconWell(type: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(34.dp)
            .drawWithCache {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val r = size.minDimension / 2f
                val center = Offset(cx, cy)
                val bgBrush = Brush.radialGradient(colors = listOf(Color(0xBC2E135A), Color(0xC2180A33)))
                val ringRadius = r - 0.5.dp.toPx()
                val ringStroke = Stroke(width = 0.85.dp.toPx())

                when (type) {
                    "compass" -> {
                        val glowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                            color = android.graphics.Color.argb(185, 168, 85, 247)
                            maskFilter = android.graphics.BlurMaskFilter(4.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
                        }
                        val diagOuter = r * 0.56f
                        val diagWaist = r * 0.12f
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
                        val starBrush = Brush.linearGradient(listOf(Color.White, Color(0xFFE9D5FF), Color(0xFF9333EA)))
                        val coreDotR = 1.8.dp.toPx()
                        onDrawBehind {
                            drawCircle(brush = bgBrush, radius = r, center = center)
                            drawCircle(color = Color(0x55D8B4FE), radius = ringRadius, center = center, style = ringStroke)
                            drawIntoCanvas { canvas ->
                                canvas.nativeCanvas.drawCircle(cx, cy, r * 0.54f, glowPaint)
                            }
                            rotate(degrees = 45f, pivot = center) {
                                drawPath(path = diagStar, color = Color(0xFFA855F7))
                            }
                            drawPath(path = starPath, brush = starBrush)
                            drawCircle(color = Color.White, radius = coreDotR, center = center)
                        }
                    }
                    "bulb" -> {
                        val glowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                            color = android.graphics.Color.argb(185, 250, 204, 21)
                            maskFilter = android.graphics.BlurMaskFilter(4.5.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
                        }
                        val glowOffsetY = cy - 1.5.dp.toPx()
                        val bulbCy = cy - r * 0.14f
                        val rayStrokeW = 1.2.dp.toPx()
                        val rays = listOf(-148f, -118f, -90f, -62f, -32f).map { deg ->
                            val rad = Math.toRadians(deg.toDouble())
                            val r1 = r * 0.48f
                            val r2 = r * 0.65f
                            Offset(cx + (Math.cos(rad) * r1).toFloat(), bulbCy + (Math.sin(rad) * r1).toFloat()) to
                                Offset(cx + (Math.cos(rad) * r2).toFloat(), bulbCy + (Math.sin(rad) * r2).toFloat())
                        }
                        val bulbR = r * 0.30f
                        val pearPath = Path().apply {
                            addOval(androidx.compose.ui.geometry.Rect(cx - bulbR, bulbCy - bulbR, cx + bulbR, bulbCy + bulbR))
                            moveTo(cx - bulbR * 0.72f, bulbCy + bulbR * 0.55f)
                            lineTo(cx - bulbR * 0.48f, bulbCy + bulbR * 1.18f)
                            lineTo(cx + bulbR * 0.48f, bulbCy + bulbR * 1.18f)
                            lineTo(cx + bulbR * 0.72f, bulbCy + bulbR * 0.55f)
                            close()
                        }
                        val pearBrush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFFBEB), Color(0xFFFDE047), Color(0xFFF59E0B)),
                            center = Offset(cx - bulbR * 0.2f, bulbCy - bulbR * 0.25f),
                            radius = (bulbR * 1.4f).coerceAtLeast(1f)
                        )
                        val capCorner = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx(), 1.dp.toPx())
                        val base1Offset = Offset(cx - bulbR * 0.46f, bulbCy + bulbR * 1.22f)
                        val base1Size = androidx.compose.ui.geometry.Size(bulbR * 0.92f, 2.dp.toPx())
                        val base2Offset = Offset(cx - bulbR * 0.32f, bulbCy + bulbR * 1.52f)
                        val base2Size = androidx.compose.ui.geometry.Size(bulbR * 0.64f, 1.8.dp.toPx())
                        onDrawBehind {
                            drawCircle(brush = bgBrush, radius = r, center = center)
                            drawCircle(color = Color(0x55D8B4FE), radius = ringRadius, center = center, style = ringStroke)
                            drawIntoCanvas { canvas ->
                                canvas.nativeCanvas.drawCircle(cx, glowOffsetY, r * 0.42f, glowPaint)
                            }
                            rays.forEach { (start, end) ->
                                drawLine(
                                    color = Color(0xFFFDE047),
                                    start = start,
                                    end = end,
                                    strokeWidth = rayStrokeW,
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                            }
                            drawPath(path = pearPath, brush = pearBrush)
                            drawRoundRect(color = Color(0xFFE2E8F0), topLeft = base1Offset, size = base1Size, cornerRadius = capCorner)
                            drawRoundRect(color = Color(0xFF94A3B8), topLeft = base2Offset, size = base2Size, cornerRadius = capCorner)
                        }
                    }
                    "pencil" -> {
                        val pw = r * 0.34f
                        val ph = r * 0.92f
                        val bodyTopLeft = Offset(cx - pw / 2f, cy - ph * 0.48f)
                        val bodySize = androidx.compose.ui.geometry.Size(pw, ph * 0.78f)
                        val bodyCorner = androidx.compose.ui.geometry.CornerRadius(1.6.dp.toPx(), 1.6.dp.toPx())
                        val bodyStroke = Stroke(width = 1.65.dp.toPx())
                        val bandStart = Offset(cx - pw / 2f, cy - ph * 0.24f)
                        val bandEnd = Offset(cx + pw / 2f, cy - ph * 0.24f)
                        val bandW = 1.5.dp.toPx()
                        val tipPath = Path().apply {
                            moveTo(cx - pw / 2f, cy + ph * 0.30f)
                            lineTo(cx, cy + ph * 0.56f)
                            lineTo(cx + pw / 2f, cy + ph * 0.30f)
                            close()
                        }
                        val tipStroke = Stroke(width = 1.65.dp.toPx(), join = androidx.compose.ui.graphics.StrokeJoin.Round)
                        onDrawBehind {
                            drawCircle(brush = bgBrush, radius = r, center = center)
                            drawCircle(color = Color(0x55D8B4FE), radius = ringRadius, center = center, style = ringStroke)
                            rotate(degrees = 45f, pivot = center) {
                                drawRoundRect(color = Color.White, topLeft = bodyTopLeft, size = bodySize, cornerRadius = bodyCorner, style = bodyStroke)
                                drawLine(color = Color.White, start = bandStart, end = bandEnd, strokeWidth = bandW)
                                drawPath(path = tipPath, color = Color.White, style = tipStroke)
                            }
                        }
                    }
                    else -> onDrawBehind {
                        drawCircle(brush = bgBrush, radius = r, center = center)
                        drawCircle(color = Color(0x55D8B4FE), radius = ringRadius, center = center, style = ringStroke)
                    }
                }
            }
    )
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

data class BookChoiceOption(val text: String, val rawLine: String)

fun parseBookChoiceOptions(rawText: String): List<BookChoiceOption> {
    return com.example.util.BookChoiceParser.parseAiBookChoices(rawText).map { parsed ->
        BookChoiceOption(text = parsed.displayText, rawLine = parsed.rawLine)
    }
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
    val icon: String,
    val isBranching: Boolean = false,
    val isRelationship: Boolean = false,
    val impactLevel: String = "orta"
)

fun cleanCopyText(rawText: String): String {
    return com.example.data.repository.EmochiRepository.cleanEmotionTags(rawText)
}
