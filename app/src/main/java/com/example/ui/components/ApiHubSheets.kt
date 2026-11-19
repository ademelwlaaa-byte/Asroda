@file:OptIn(ExperimentalLayoutApi::class)
package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.local.CustomProviderEntity
import com.example.data.managers.ApiCatalogCategory
import com.example.data.managers.ApiProviderCatalog
import com.example.data.managers.CatalogProviderSpec
import com.example.data.repository.EmochiRepository
import com.example.ui.screens.aetheriaBackdropBlur
import com.example.ui.screens.morCamGlassSurface
import com.example.ui.screens.morCamOuterGlow
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.InterFontFamily
import com.example.util.Llm7RateLimitTracker
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════════════════════════════════════════
// SHARED MOR NEON COSMIC GLASS PRIMITIVES FOR GLOBAL SETTINGS & API HUB
// ═══════════════════════════════════════════════════════════════════════════════

private var cachedFrostedGlobalSettingsBackdrop: ImageBitmap? = null

internal fun createFrostedGlobalSettingsBackdropBitmap(context: android.content.Context): ImageBitmap {
    cachedFrostedGlobalSettingsBackdrop?.let { return it }
    return try {
        val opts = android.graphics.BitmapFactory.Options().apply {
            inSampleSize = 2
            inPreferredConfig = android.graphics.Bitmap.Config.RGB_565
        }
        val raw = android.graphics.BitmapFactory.decodeResource(
            context.resources,
            R.drawable.img_global_settings_cosmic_bg,
            opts
        ) ?: return ImageBitmap(1, 1)

        val w = 360
        val h = ((raw.height.toFloat() / raw.width.toFloat().coerceAtLeast(1f)) * w).toInt().coerceIn(360, 1080)
        val scaled = android.graphics.Bitmap.createScaledBitmap(raw, w, h, true)
        if (raw !== scaled) raw.recycle()

        val pixels = IntArray(w * h)
        scaled.getPixels(pixels, 0, w, 0, 0, w, h)
        val tmp = IntArray(w * h)
        val radius = 4
        val div = radius * 2 + 1

        repeat(2) {
            for (y in 0 until h) {
                var rSum = 0; var gSum = 0; var bSum = 0
                val rowOff = y * w
                for (i in -radius..radius) {
                    val c = pixels[rowOff + i.coerceIn(0, w - 1)]
                    rSum += (c shr 16) and 0xFF
                    gSum += (c shr 8) and 0xFF
                    bSum += c and 0xFF
                }
                for (x in 0 until w) {
                    tmp[rowOff + x] = (0xFF shl 24) or ((rSum / div) shl 16) or ((gSum / div) shl 8) or (bSum / div)
                    val outIdx = (x - radius).coerceIn(0, w - 1)
                    val inIdx = (x + radius + 1).coerceIn(0, w - 1)
                    val cOut = pixels[rowOff + outIdx]
                    val cIn = pixels[rowOff + inIdx]
                    rSum += ((cIn shr 16) and 0xFF) - ((cOut shr 16) and 0xFF)
                    gSum += ((cIn shr 8) and 0xFF) - ((cOut shr 8) and 0xFF)
                    bSum += (cIn and 0xFF) - (cOut and 0xFF)
                }
            }
            for (x in 0 until w) {
                var rSum = 0; var gSum = 0; var bSum = 0
                for (i in -radius..radius) {
                    val c = tmp[i.coerceIn(0, h - 1) * w + x]
                    rSum += (c shr 16) and 0xFF
                    gSum += (c shr 8) and 0xFF
                    bSum += c and 0xFF
                }
                for (y in 0 until h) {
                    pixels[y * w + x] = (0xFF shl 24) or ((rSum / div) shl 16) or ((gSum / div) shl 8) or (bSum / div)
                    val outIdx = (y - radius).coerceIn(0, h - 1)
                    val inIdx = (y + radius + 1).coerceIn(0, h - 1)
                    val cOut = tmp[outIdx * w + x]
                    val cIn = tmp[inIdx * w + x]
                    rSum += ((cIn shr 16) and 0xFF) - ((cOut shr 16) and 0xFF)
                    gSum += ((cIn shr 8) and 0xFF) - ((cOut shr 8) and 0xFF)
                    bSum += (cIn and 0xFF) - (cOut and 0xFF)
                }
            }
        }
        val blurred = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888)
        blurred.setPixels(pixels, 0, w, 0, 0, w, h)
        scaled.recycle()
        blurred.asImageBitmap().also { cachedFrostedGlobalSettingsBackdrop = it }
    } catch (_: Exception) {
        ImageBitmap(1, 1)
    }
}

internal object GlobalMorCamColors {
    val PearlText = Color(0xFFFDFBFF)
    val LavenderSubtext = Color(0xFFD2C4E8)
    val MutedLavender = Color(0xFFAE9CC9)
    val ActiveViolet = Color(0xFF8B5CF6)
    val DeepAmethyst = Color(0xFF6D28D9)
    val BrightViolet = Color(0xFFC084FC)
    val NeonOrchid = Color(0xFFA855F7)
    val SoftEmerald = Color(0xFF34D399)
    val EmeraldRim = Color(0xFF14B8A6)
    val SoftRose = Color(0xFFFDA4AF)
    val SoftAmber = Color(0xFFFDE68A)
    val SmokeFill = Color(0xC8110524)
    val DeepSmokeFill = Color(0xDA0B031A)
    val RimGradient = Brush.linearGradient(
        listOf(
            Color(0xCCD8B4FE),
            Color(0xCC9333EA),
            Color(0xB87C3AED),
            Color(0xCCA855F7),
            Color(0xCCD8B4FE)
        )
    )
    val ActiveRimGradient = Brush.linearGradient(
        listOf(
            Color(0xFFF3E8FF),
            Color(0xFFD8B4FE),
            Color(0xFFA855F7),
            Color(0xFFE9D5FF)
        )
    )
    val SubtleTabRimGradient = Brush.linearGradient(
        listOf(
            Color(0x78A78BFA),
            Color(0x486D28D9),
            Color(0x668B5CF6)
        )
    )
}

/**
 * Draws a sleek, tight, pure electric-violet neon outer halo around glass cards,
 * active tabs, and orbs — rich deep purple glow without milky white or pink fog.
 */
internal fun Modifier.morNeonDoubleHalo(
    cornerRadius: Dp,
    outerColor: Color = Color(0xFF7C3AED),
    midColor: Color = Color(0xFFA855F7),
    coreColor: Color = Color(0xFFD8B4FE),
    intensity: Float = 0.55f,
    outerBlur: Dp = 10.dp,
    midBlur: Dp = 4.dp
): Modifier = this.drawBehind {
    val w = size.width
    val h = size.height
    if (w <= 1f || h <= 1f || intensity <= 0.01f) return@drawBehind
    val cr = cornerRadius.toPx().coerceAtMost(minOf(w, h) / 2f)
    val clamped = intensity.coerceIn(0f, 1.1f)

    drawIntoCanvas { canvas ->
        val native = canvas.nativeCanvas

        // Layer 1: Tight deep-violet neon glow hugging the rim
        val outerPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 3.2.dp.toPx()
            color = android.graphics.Color.argb(
                (0.50f * clamped).coerceIn(0f, 1f).let { (it * 255).toInt() },
                (outerColor.red * 255).toInt(),
                (outerColor.green * 255).toInt(),
                (outerColor.blue * 255).toInt()
            )
            maskFilter = android.graphics.BlurMaskFilter(
                outerBlur.toPx().coerceAtLeast(1f),
                android.graphics.BlurMaskFilter.Blur.NORMAL
            )
        }
        native.drawRoundRect(0f, 0f, w, h, cr, cr, outerPaint)

        // Layer 2: Crisp electric-purple inner neon edge bloom
        val midPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 1.6.dp.toPx()
            color = android.graphics.Color.argb(
                (0.58f * clamped).coerceIn(0f, 1f).let { (it * 255).toInt() },
                (midColor.red * 255).toInt(),
                (midColor.green * 255).toInt(),
                (midColor.blue * 255).toInt()
            )
            maskFilter = android.graphics.BlurMaskFilter(
                midBlur.toPx().coerceAtLeast(1f),
                android.graphics.BlurMaskFilter.Blur.NORMAL
            )
        }
        native.drawRoundRect(0f, 0f, w, h, cr, cr, midPaint)

        // Optional Layer 3 only for strongly highlighted elements (active tab / save pill)
        if (clamped > 0.72f) {
            val coreAlpha = ((clamped - 0.72f) * 1.4f).coerceIn(0f, 0.55f)
            val corePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 1.1.dp.toPx()
                color = android.graphics.Color.argb(
                    (coreAlpha * 255).toInt(),
                    (coreColor.red * 255).toInt(),
                    (coreColor.green * 255).toInt(),
                    (coreColor.blue * 255).toInt()
                )
                maskFilter = android.graphics.BlurMaskFilter(
                    2.2.dp.toPx(),
                    android.graphics.BlurMaskFilter.Blur.NORMAL
                )
            }
            native.drawRoundRect(0f, 0f, w, h, cr, cr, corePaint)
        }
    }
}

/**
 * Draws a subtle deep-violet inner glass bevel and razor-thin specular border glints
 * along the 1.5dp rim (ZERO cloudy white radial blobs inside the card body).
 */
internal fun Modifier.morNeonGlassFlares(
    cornerRadius: Dp = 20.dp,
    topAlpha: Float = 0.52f,
    bottomAlpha: Float = 0.36f,
    innerBevelAlpha: Float = 0.22f,
    topXFraction: Float = 0.26f,
    bottomXFraction: Float = 0.78f
): Modifier = this.drawWithContent {
    val w = size.width
    val h = size.height
    if (w > 2f && h > 2f && innerBevelAlpha > 0.01f) {
        val cr = cornerRadius.toPx().coerceAtMost(minOf(w, h) / 2f)
        drawIntoCanvas { canvas ->
            val innerBevel = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 3.2.dp.toPx()
                color = android.graphics.Color.argb(
                    (innerBevelAlpha.coerceIn(0f, 1f) * 255).toInt(),
                    139,
                    92,
                    246
                )
                maskFilter = android.graphics.BlurMaskFilter(
                    5.5.dp.toPx(),
                    android.graphics.BlurMaskFilter.Blur.NORMAL
                )
            }
            canvas.nativeCanvas.drawRoundRect(0f, 0f, w, h, cr, cr, innerBevel)
        }
    }

    drawContent()

    if (w > 2f && h > 2f) {
        // Razor-thin specular highlight right ON the top border edge
        if (topAlpha > 0.01f) {
            val tx = w * topXFraction
            val halfLen = (w * 0.16f).coerceIn(22.dp.toPx(), 64.dp.toPx())
            val startX = (tx - halfLen).coerceAtLeast(cornerRadius.toPx() * 0.5f)
            val endX = (tx + halfLen).coerceAtMost(w - cornerRadius.toPx() * 0.5f)
            if (endX > startX) {
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFFD8B4FE).copy(alpha = (topAlpha * 0.75f).coerceIn(0f, 1f)),
                            Color(0xFFFDF7FF).copy(alpha = topAlpha.coerceIn(0f, 1f)),
                            Color(0xFFD8B4FE).copy(alpha = (topAlpha * 0.75f).coerceIn(0f, 1f)),
                            Color.Transparent
                        ),
                        startX = startX,
                        endX = endX
                    ),
                    start = Offset(startX, 0.8.dp.toPx()),
                    end = Offset(endX, 0.8.dp.toPx()),
                    strokeWidth = 1.7.dp.toPx()
                )
            }
        }

        // Razor-thin subtle highlight right ON the bottom border edge
        if (bottomAlpha > 0.01f) {
            val bx = w * bottomXFraction
            val halfLen = (w * 0.14f).coerceIn(20.dp.toPx(), 54.dp.toPx())
            val startX = (bx - halfLen).coerceAtLeast(cornerRadius.toPx() * 0.5f)
            val endX = (bx + halfLen).coerceAtMost(w - cornerRadius.toPx() * 0.5f)
            if (endX > startX) {
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFFC084FC).copy(alpha = (bottomAlpha * 0.70f).coerceIn(0f, 1f)),
                            Color(0xFFF3E8FF).copy(alpha = bottomAlpha.coerceIn(0f, 1f)),
                            Color(0xFFC084FC).copy(alpha = (bottomAlpha * 0.70f).coerceIn(0f, 1f)),
                            Color.Transparent
                        ),
                        startX = startX,
                        endX = endX
                    ),
                    start = Offset(startX, h - 0.8.dp.toPx()),
                    end = Offset(endX, h - 0.8.dp.toPx()),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
        }
    }
}

@Composable
internal fun GlobalGlassCard(
    rootSize: IntSize,
    blurredBg: ImageBitmap,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    highlighted: Boolean = false,
    topFlareFraction: Float = 0.26f,
    bottomFlareFraction: Float = 0.78f,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 11.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Column(
        modifier = modifier
            .morNeonDoubleHalo(
                cornerRadius = cornerRadius,
                outerColor = if (highlighted) Color(0xFF9333EA) else Color(0xFF7C3AED),
                midColor = if (highlighted) Color(0xFFC084FC) else Color(0xFF9333EA),
                coreColor = Color(0xFFE9D5FF),
                intensity = if (highlighted) 0.72f else 0.50f,
                outerBlur = if (highlighted) 12.dp else 9.dp,
                midBlur = if (highlighted) 5.dp else 4.dp
            )
            .clip(shape)
            .aetheriaBackdropBlur(rootSize, blurredBg, alpha = 0.60f)
            .background(
                Brush.verticalGradient(
                    if (highlighted) {
                        listOf(Color(0xB8210B45), Color(0xCC13062B), Color(0xC21B093A))
                    } else {
                        listOf(Color(0xBA17082E), Color(0xCF0E0420), Color(0xC4140728))
                    }
                )
            )
            .morNeonGlassFlares(
                cornerRadius = cornerRadius,
                topAlpha = if (highlighted) 0.62f else 0.48f,
                bottomAlpha = if (highlighted) 0.45f else 0.34f,
                innerBevelAlpha = if (highlighted) 0.28f else 0.20f,
                topXFraction = topFlareFraction,
                bottomXFraction = bottomFlareFraction
            )
            .border(
                BorderStroke(
                    width = if (highlighted) 1.4.dp else 1.25.dp,
                    brush = if (highlighted) GlobalMorCamColors.ActiveRimGradient else GlobalMorCamColors.RimGradient
                ),
                shape
            )
            .padding(contentPadding),
        content = content
    )
}

@Composable
internal fun GlobalGlassIconOrb(
    rootSize: IntSize,
    blurredBg: ImageBitmap,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    highlighted: Boolean = false,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.91f else 1f,
        animationSpec = tween(100),
        label = "orbScale"
    )
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .morNeonDoubleHalo(
                cornerRadius = size / 2,
                outerColor = if (highlighted) Color(0xFF9333EA) else Color(0xFF7C3AED),
                midColor = if (highlighted) Color(0xFFC084FC) else Color(0xFF9333EA),
                coreColor = Color(0xFFE9D5FF),
                intensity = if (highlighted) 0.78f else 0.46f,
                outerBlur = if (highlighted) 11.dp else 8.dp,
                midBlur = if (highlighted) 5.dp else 3.5.dp
            )
            .clip(CircleShape)
            .aetheriaBackdropBlur(rootSize, blurredBg, alpha = 0.55f)
            .background(
                if (highlighted) {
                    Brush.radialGradient(
                        listOf(Color(0xEE6D28D9), Color(0xEE4C1D95), Color(0xEE2B0B5E))
                    )
                } else {
                    Brush.radialGradient(
                        listOf(Color(0xC81D0A3B), Color(0xDA0D041E))
                    )
                }
            )
            .border(
                BorderStroke(
                    width = if (highlighted) 1.45.dp else 1.25.dp,
                    brush = if (highlighted) GlobalMorCamColors.ActiveRimGradient else GlobalMorCamColors.RimGradient
                ),
                CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
internal fun GlobalMorCamSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 25.dp else 3.dp,
        animationSpec = tween(180),
        label = "morCamSwitchOffset"
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) Color(0xFFE9D5FF) else Color(0x999F7AEA),
        animationSpec = tween(180),
        label = "morCamSwitchBorder"
    )
    val shape = RoundedCornerShape(15.dp)

    Box(
        modifier = modifier
            .size(width = 50.dp, height = 28.dp)
            .then(
                if (checked) {
                    Modifier.morNeonDoubleHalo(
                        cornerRadius = 14.dp,
                        outerColor = Color(0xFF7C3AED),
                        midColor = Color(0xFFA855F7),
                        coreColor = Color(0xFFE9D5FF),
                        intensity = 0.58f,
                        outerBlur = 8.dp,
                        midBlur = 4.dp
                    )
                } else Modifier
            )
            .clip(shape)
            .background(
                if (checked) {
                    Brush.horizontalGradient(
                        listOf(Color(0xF06D28D9), Color(0xF58B5CF6), Color(0xF0A855F7))
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(Color(0xC4130628), Color(0xBA1E0B3D))
                    )
                }
            )
            .border(1.15.dp, borderColor, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .size(22.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        if (checked) listOf(Color(0xFFFFFFFF), Color(0xFFF8F4FF))
                        else listOf(Color(0xFFF5EEFF), Color(0xFFD8CCF0))
                    )
                )
        )
    }
}

@Composable
internal fun GlobalGlassInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    singleLine: Boolean = true,
    isSecret: Boolean = false,
    showSecret: Boolean = false,
    onToggleSecret: (() -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                color = GlobalMorCamColors.LavenderSubtext,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = InterFontFamily
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x780F0522))
                .border(1.05.dp, GlobalMorCamColors.RimGradient, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (leadingIcon != null) leadingIcon()
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = GlobalMorCamColors.MutedLavender.copy(alpha = 0.72f),
                        fontSize = 13.sp,
                        fontFamily = InterFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = singleLine,
                    visualTransformation = if (isSecret && !showSecret) PasswordVisualTransformation() else VisualTransformation.None,
                    textStyle = TextStyle(
                        color = GlobalMorCamColors.PearlText,
                        fontSize = 13.5.sp,
                        fontFamily = InterFontFamily
                    ),
                    cursorBrush = SolidColor(GlobalMorCamColors.BrightViolet),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (isSecret && onToggleSecret != null) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onToggleSecret),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (showSecret) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showSecret) "Anahtarı Gizle" else "Anahtarı Göster",
                        tint = GlobalMorCamColors.BrightViolet,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun GlobalGlassPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !loading) 0.96f else 1f,
        animationSpec = tween(100),
        label = "primaryBtnScale"
    )
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .then(
                if (enabled) Modifier.morCamCosmicGlow(
                    cornerRadius = 24.dp,
                    glowColor = Color(0xFFA855F7),
                    alpha = 0.45f,
                    blurDp = 10.dp
                ) else Modifier
            )
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    if (enabled) listOf(Color(0xE07C3AED), Color(0xE89333EA), Color(0xE06D28D9))
                    else listOf(Color(0x553B245E), Color(0x552E1A4D))
                )
            )
            .border(
                BorderStroke(
                    width = 1.2.dp,
                    brush = if (enabled) GlobalMorCamColors.ActiveRimGradient else MorCamTokens.SubtleRimGradient
                ),
                shape
            )
            .clickable(
                enabled = enabled && !loading,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = GlobalMorCamColors.PearlText,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(
                text = text,
                color = if (enabled) GlobalMorCamColors.PearlText else GlobalMorCamColors.MutedLavender,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = InterFontFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun GlobalGlassSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !loading) 0.96f else 1f,
        animationSpec = tween(100),
        label = "secondaryBtnScale"
    )
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .background(
                Brush.horizontalGradient(listOf(Color(0x701B0B38), Color(0x78120628)))
            )
            .border(1.05.dp, GlobalMorCamColors.RimGradient, shape)
            .clickable(
                enabled = enabled && !loading,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = GlobalMorCamColors.BrightViolet,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(
                text = text,
                color = if (enabled) GlobalMorCamColors.PearlText else GlobalMorCamColors.MutedLavender,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = InterFontFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FullScreenMorCamSheetContainer(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    BackHandler { onDismiss() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070212))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Intercept touches */ }
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_global_settings_cosmic_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0x22080215), Color(0x140F0424), Color(0x34060112))
                    )
                )
        )
        content()
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 1. SEARCHABLE API PROVIDER CATALOG DIALOG (28+ CONNECTABLE SITES)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
internal fun ApiCatalogSearchDialog(
    rootSize: IntSize,
    blurredBg: ImageBitmap,
    configuredBuiltinSlots: Set<String>,
    configuredCustomUrls: Set<String>,
    onSelectProvider: (CatalogProviderSpec) -> Unit,
    onOpenCustomApi: () -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ApiCatalogCategory.ALL) }

    val filteredProviders = remember(searchQuery, selectedCategory) {
        val q = searchQuery.trim().lowercase()
        ApiProviderCatalog.catalogProviders.filter { spec ->
            val catMatch = selectedCategory == ApiCatalogCategory.ALL || selectedCategory in spec.categories
            val queryMatch = q.isBlank() ||
                spec.name.lowercase().contains(q) ||
                spec.shortDesc.lowercase().contains(q) ||
                spec.badge.lowercase().contains(q) ||
                spec.baseUrl.lowercase().contains(q) ||
                spec.popularModels.any { it.lowercase().contains(q) }
            catMatch && queryMatch
        }
    }

    FullScreenMorCamSheetContainer(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlobalGlassIconOrb(
                    rootSize = rootSize,
                    blurredBg = blurredBg,
                    onClick = onDismiss,
                    modifier = Modifier.testTag("catalog_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Geri",
                        tint = GlobalMorCamColors.PearlText,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "API Sağlayıcıları",
                        color = GlobalMorCamColors.PearlText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DisplaySerifFontFamily
                    )
                    Text(
                        text = "${ApiProviderCatalog.catalogProviders.size} servis • Bir sağlayıcı seçip anahtar & model ekle",
                        color = GlobalMorCamColors.LavenderSubtext,
                        fontSize = 11.5.sp,
                        fontFamily = InterFontFamily
                    )
                }
            }

            // Search bar
            GlobalGlassInputField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Sağlayıcı, site veya model ara (ör. DeepSeek, Groq, Claude)...",
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("api_catalog_search_input"),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = GlobalMorCamColors.BrightViolet,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            // Category chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ApiCatalogCategory.entries.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .heightIn(min = 36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (isSelected) Color(0xCC7C3AED) else Color(0x6E180B30)
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFFE9D5FF) else Color(0x55B794F4),
                                RoundedCornerShape(18.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat.label,
                            color = if (isSelected) GlobalMorCamColors.PearlText else GlobalMorCamColors.LavenderSubtext,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontFamily = InterFontFamily
                        )
                    }
                }
            }

            // Provider catalog list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredProviders, key = { it.id }) { spec ->
                    val isAlreadyAdded = (spec.builtinSlot != null && spec.builtinSlot in configuredBuiltinSlots) ||
                        configuredCustomUrls.any { it.equals(spec.baseUrl.trimEnd('/'), ignoreCase = true) }

                    GlobalGlassCard(
                        rootSize = rootSize,
                        blurredBg = blurredBg,
                        cornerRadius = 18.dp,
                        highlighted = isAlreadyAdded,
                        contentPadding = PaddingValues(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectProvider(spec) }
                            .testTag("catalog_item_${spec.id}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isAlreadyAdded) Color(0x446EE7B7) else Color(0x448B5CF6)
                                    )
                                    .border(
                                        1.dp,
                                        if (isAlreadyAdded) Color(0xAA6EE7B7) else Color(0x88C084FC),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = spec.name.take(2).uppercase(),
                                    color = GlobalMorCamColors.PearlText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = InterFontFamily
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = spec.name,
                                        color = GlobalMorCamColors.PearlText,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = InterFontFamily,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isAlreadyAdded) Color(0x336EE7B7) else Color(0x33C084FC)
                                            )
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isAlreadyAdded) "Ekli" else spec.badge,
                                            color = if (isAlreadyAdded) GlobalMorCamColors.SoftEmerald else Color(0xFFE9D5FF),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = InterFontFamily
                                        )
                                    }
                                }
                                Text(
                                    text = spec.shortDesc,
                                    color = GlobalMorCamColors.LavenderSubtext,
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.5.sp,
                                    fontFamily = InterFontFamily,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Önerilen: ${spec.defaultModel}",
                                    color = GlobalMorCamColors.MutedLavender,
                                    fontSize = 10.5.sp,
                                    fontFamily = InterFontFamily,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = GlobalMorCamColors.BrightViolet,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Bottom shortcut for Custom API if user didn't find their site
                item {
                    GlobalGlassCard(
                        rootSize = rootSize,
                        blurredBg = blurredBg,
                        cornerRadius = 18.dp,
                        contentPadding = PaddingValues(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismiss()
                                onOpenCustomApi()
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = GlobalMorCamColors.BrightViolet,
                                modifier = Modifier.size(22.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Aradığın servis listede yok mu?",
                                    color = GlobalMorCamColors.PearlText,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = InterFontFamily
                                )
                                Text(
                                    text = "Özel (Custom) OpenAI veya Anthropic uyumlu URL ekle",
                                    color = GlobalMorCamColors.LavenderSubtext,
                                    fontSize = 11.5.sp,
                                    fontFamily = InterFontFamily
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = GlobalMorCamColors.BrightViolet
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 2. CATALOG PROVIDER CONFIGURE DIALOG (ENTER KEY + PICK/TYPE MODEL + TEST)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
internal fun ApiProviderConfigureDialog(
    spec: CatalogProviderSpec,
    initialApiKey: String,
    initialModel: String,
    isEditingExisting: Boolean,
    rootSize: IntSize,
    blurredBg: ImageBitmap,
    onTestConnection: suspend (baseUrl: String, apiKey: String, model: String, apiFormat: String) -> EmochiRepository.ProviderTestResult,
    onFetchModels: suspend (baseUrl: String, apiKey: String) -> List<String>,
    onConfirm: (apiKey: String, modelName: String, supportsTools: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var apiKey by remember(spec.id, initialApiKey) { mutableStateOf(initialApiKey) }
    var modelName by remember(spec.id, initialModel) {
        mutableStateOf(initialModel.ifBlank { spec.defaultModel })
    }
    var showKey by remember { mutableStateOf(false) }
    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<EmochiRepository.ProviderTestResult?>(null) }
    var isFetchingModels by remember { mutableStateOf(false) }
    var fetchedModels by remember { mutableStateOf<List<String>>(emptyList()) }
    var fetchInfo by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val canSave = modelName.isNotBlank() && (!spec.requiresApiKey || apiKey.isNotBlank() || isEditingExisting)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 18.dp)
        ) {
            GlobalGlassCard(
                rootSize = rootSize,
                blurredBg = blurredBg,
                cornerRadius = 24.dp,
                highlighted = true,
                contentPadding = PaddingValues(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = spec.name,
                                color = GlobalMorCamColors.PearlText,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DisplaySerifFontFamily
                            )
                            Text(
                                text = spec.shortDesc,
                                color = GlobalMorCamColors.LavenderSubtext,
                                fontSize = 11.5.sp,
                                lineHeight = 15.sp,
                                fontFamily = InterFontFamily
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x55180B30))
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Kapat",
                                tint = GlobalMorCamColors.PearlText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Key input
                    GlobalGlassInputField(
                        value = apiKey,
                        onValueChange = { apiKey = it; testResult = null },
                        label = if (spec.requiresApiKey) {
                            if (isEditingExisting && initialApiKey.isBlank()) "API Anahtarı (Değiştirmek istemiyorsan boş bırak)"
                            else "API Anahtarı"
                        } else "API Anahtarı (İsteğe bağlı)",
                        placeholder = if (spec.requiresApiKey) "Anahtarını buraya yapıştır..." else "Yerel sunucuda boş bırakılabilir",
                        isSecret = true,
                        showSecret = showKey,
                        onToggleSecret = { showKey = !showKey },
                        modifier = Modifier.testTag("provider_apikey_input"),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = GlobalMorCamColors.BrightViolet,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    )

                    Text(
                        text = "Anahtar adresi: ${spec.keyHint}",
                        color = GlobalMorCamColors.MutedLavender,
                        fontSize = 10.5.sp,
                        fontFamily = InterFontFamily
                    )

                    // Model input + Fetch models button
                    GlobalGlassInputField(
                        value = modelName,
                        onValueChange = { modelName = it; testResult = null },
                        label = "Model Seçimi veya Model Kodu",
                        placeholder = spec.defaultModel,
                        modifier = Modifier.testTag("provider_model_input")
                    )

                    // Popular models flow chips
                    Text(
                        text = "Popüler Modeller (Dokun & Seç):",
                        color = GlobalMorCamColors.LavenderSubtext,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = InterFontFamily
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val combinedModels = remember(spec.popularModels, fetchedModels) {
                            (spec.popularModels + fetchedModels.take(18)).distinct()
                        }
                        combinedModels.forEach { m ->
                            val isSelected = modelName.trim() == m
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color(0xCC7C3AED) else Color(0x6615082A))
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFFE9D5FF) else Color(0x55B794F4),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { modelName = m; testResult = null }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = m,
                                    color = if (isSelected) GlobalMorCamColors.PearlText else GlobalMorCamColors.LavenderSubtext,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontFamily = InterFontFamily,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    if (spec.apiFormat == "openai") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x55221242))
                                    .border(0.9.dp, Color(0x77C084FC), RoundedCornerShape(12.dp))
                                    .clickable(enabled = !isFetchingModels) {
                                        scope.launch {
                                            isFetchingModels = true
                                            fetchInfo = null
                                            val list = onFetchModels(spec.baseUrl, apiKey)
                                            fetchedModels = list
                                            fetchInfo = if (list.isNotEmpty()) "${list.size} canlı model alındı" else "Canlı liste alınamadı (elle model yazabilirsin)"
                                            isFetchingModels = false
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (isFetchingModels) {
                                        CircularProgressIndicator(
                                            color = GlobalMorCamColors.BrightViolet,
                                            strokeWidth = 1.6.dp,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.CloudDownload,
                                            contentDescription = null,
                                            tint = GlobalMorCamColors.BrightViolet,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    Text(
                                        text = "Canlı Modelleri Çek",
                                        color = GlobalMorCamColors.PearlText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = InterFontFamily
                                    )
                                }
                            }
                            if (!fetchInfo.isNullOrBlank()) {
                                Text(
                                    text = fetchInfo!!,
                                    color = GlobalMorCamColors.LavenderSubtext,
                                    fontSize = 10.5.sp,
                                    fontFamily = InterFontFamily,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }

                    // Test Result banner
                    testResult?.let { res ->
                        TestResultGlassBanner(res)
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlobalGlassSecondaryButton(
                            text = "Test Et",
                            loading = isTesting,
                            enabled = modelName.isNotBlank() && (!spec.requiresApiKey || apiKey.isNotBlank()),
                            onClick = {
                                scope.launch {
                                    isTesting = true
                                    testResult = onTestConnection(spec.baseUrl, apiKey, modelName.trim(), spec.apiFormat)
                                    isTesting = false
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("provider_test_button")
                        )
                        GlobalGlassPrimaryButton(
                            text = if (isEditingExisting) "Güncelle" else "Ekle & Sıraya Al",
                            enabled = canSave,
                            onClick = {
                                onConfirm(
                                    apiKey.trim(),
                                    modelName.trim(),
                                    testResult?.supportsFunctionCalling ?: true
                                )
                            },
                            modifier = Modifier
                                .weight(1.45f)
                                .testTag("provider_confirm_button")
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 3. CUSTOM (ÖZEL) API ADD / EDIT DIALOG
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
internal fun CustomApiEditorDialog(
    editingProvider: CustomProviderEntity? = null,
    rootSize: IntSize,
    blurredBg: ImageBitmap,
    onTestConnection: suspend (baseUrl: String, apiKey: String, model: String, apiFormat: String) -> EmochiRepository.ProviderTestResult,
    onFetchModels: suspend (baseUrl: String, apiKey: String) -> List<String>,
    onSave: (label: String, baseUrl: String, apiKey: String, modelName: String, apiFormat: String, supportsTools: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var label by remember(editingProvider?.id) { mutableStateOf(editingProvider?.label ?: "") }
    var baseUrl by remember(editingProvider?.id) { mutableStateOf(editingProvider?.baseUrl ?: "https://") }
    var apiKey by remember(editingProvider?.id) { mutableStateOf("") }
    var modelName by remember(editingProvider?.id) { mutableStateOf(editingProvider?.modelName ?: "") }
    var apiFormat by remember(editingProvider?.id) { mutableStateOf(editingProvider?.apiFormat ?: "openai") }
    var showKey by remember { mutableStateOf(false) }
    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<EmochiRepository.ProviderTestResult?>(null) }
    var isFetching by remember { mutableStateOf(false) }
    var fetchedModels by remember { mutableStateOf<List<String>>(emptyList()) }
    val scope = rememberCoroutineScope()

    val isEditing = editingProvider != null
    val canSave = baseUrl.trim().length > 8 && modelName.isNotBlank()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 18.dp)
        ) {
            GlobalGlassCard(
                rootSize = rootSize,
                blurredBg = blurredBg,
                cornerRadius = 24.dp,
                highlighted = true,
                contentPadding = PaddingValues(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEditing) "Özel API'yi Düzenle" else "Özel (Custom) API Ekle",
                                color = GlobalMorCamColors.PearlText,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = DisplaySerifFontFamily
                            )
                            Text(
                                text = "Herhangi bir OpenAI veya Anthropic uyumlu sunucuya bağlan.",
                                color = GlobalMorCamColors.LavenderSubtext,
                                fontSize = 11.5.sp,
                                fontFamily = InterFontFamily
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x55180B30))
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Kapat",
                                tint = GlobalMorCamColors.PearlText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    GlobalGlassInputField(
                        value = label,
                        onValueChange = { label = it },
                        label = "Görünen Ad",
                        placeholder = "Ör. Kendi Proxy Sunucum, Özel DeepSeek",
                        modifier = Modifier.testTag("custom_api_label_input")
                    )

                    // Protocol selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("openai" to "OpenAI Uyumlu", "anthropic" to "Anthropic Uyumlu").forEach { (fmt, title) ->
                            val selected = apiFormat == fmt
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) Color(0xCC7C3AED) else Color(0x6615082A))
                                    .border(
                                        1.dp,
                                        if (selected) Color(0xFFE9D5FF) else Color(0x55B794F4),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { apiFormat = fmt; testResult = null },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    color = if (selected) GlobalMorCamColors.PearlText else GlobalMorCamColors.LavenderSubtext,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    fontFamily = InterFontFamily
                                )
                            }
                        }
                    }

                    GlobalGlassInputField(
                        value = baseUrl,
                        onValueChange = { baseUrl = it; testResult = null },
                        label = "Base URL",
                        placeholder = "https://api.orneksite.com/v1",
                        modifier = Modifier.testTag("custom_api_url_input")
                    )

                    GlobalGlassInputField(
                        value = apiKey,
                        onValueChange = { apiKey = it; testResult = null },
                        label = if (isEditing) "Yeni API Anahtarı (Aynı kalsın istiyorsan boş bırak)" else "API Anahtarı",
                        placeholder = "sk-...",
                        isSecret = true,
                        showSecret = showKey,
                        onToggleSecret = { showKey = !showKey },
                        modifier = Modifier.testTag("custom_api_key_input")
                    )

                    GlobalGlassInputField(
                        value = modelName,
                        onValueChange = { modelName = it; testResult = null },
                        label = "Model Kodu / Adı",
                        placeholder = "Ör. deepseek-chat, llama-3.3-70b-instruct",
                        modifier = Modifier.testTag("custom_api_model_input")
                    )

                    if (apiFormat == "openai") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x55221242))
                                    .border(0.9.dp, Color(0x77C084FC), RoundedCornerShape(12.dp))
                                    .clickable(enabled = !isFetching && baseUrl.trim().length > 8) {
                                        scope.launch {
                                            isFetching = true
                                            fetchedModels = onFetchModels(baseUrl.trim(), apiKey.trim())
                                            isFetching = false
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (isFetching) {
                                        CircularProgressIndicator(
                                            color = GlobalMorCamColors.BrightViolet,
                                            strokeWidth = 1.6.dp,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.CloudDownload,
                                            contentDescription = null,
                                            tint = GlobalMorCamColors.BrightViolet,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    Text(
                                        text = "Sunucudan Modelleri Çek",
                                        color = GlobalMorCamColors.PearlText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = InterFontFamily
                                    )
                                }
                            }
                        }

                        if (fetchedModels.isNotEmpty()) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                fetchedModels.take(14).forEach { m ->
                                    val selected = modelName.trim() == m
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (selected) Color(0xCC7C3AED) else Color(0x6615082A))
                                            .border(1.dp, if (selected) Color(0xFFE9D5FF) else Color(0x55B794F4), RoundedCornerShape(10.dp))
                                            .clickable { modelName = m; testResult = null }
                                            .padding(horizontal = 9.dp, vertical = 5.dp)
                                    ) {
                                        Text(m, color = GlobalMorCamColors.PearlText, fontSize = 10.5.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Keystore encryption hint
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x441B0D36))
                            .border(0.8.dp, Color(0x55C084FC), RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = GlobalMorCamColors.SoftAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "API anahtarın cihazda Android Keystore ile şifrelenerek yalnızca girdiğin adrese gönderilir.",
                            color = GlobalMorCamColors.LavenderSubtext,
                            fontSize = 10.5.sp,
                            lineHeight = 14.sp,
                            fontFamily = InterFontFamily
                        )
                    }

                    testResult?.let { TestResultGlassBanner(it) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlobalGlassSecondaryButton(
                            text = "Test Et",
                            loading = isTesting,
                            enabled = canSave,
                            onClick = {
                                scope.launch {
                                    isTesting = true
                                    testResult = onTestConnection(baseUrl.trim(), apiKey.trim(), modelName.trim(), apiFormat)
                                    isTesting = false
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        GlobalGlassPrimaryButton(
                            text = if (isEditing) "Güncelle" else "Kaydet & Ekle",
                            enabled = canSave,
                            onClick = {
                                onSave(
                                    label.trim().ifBlank { modelName.trim() },
                                    baseUrl.trim(),
                                    apiKey.trim(),
                                    modelName.trim(),
                                    apiFormat,
                                    testResult?.supportsFunctionCalling ?: true
                                )
                            },
                            modifier = Modifier
                                .weight(1.45f)
                                .testTag("custom_api_save_button")
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 4. FREE KEYLESS PROVIDERS SUB-MENU (POLLINATIONS, LLM7, OVH AI)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
internal fun FreeKeylessProvidersDialog(
    rootSize: IntSize,
    blurredBg: ImageBitmap,
    prioritizeFreeKeyless: Boolean,
    enablePollinations: Boolean,
    onTogglePollinations: (Boolean) -> Unit,
    pollinationsModel: String,
    onSelectPollinationsModel: (String) -> Unit,
    enableLlm7: Boolean,
    onToggleLlm7: (Boolean) -> Unit,
    enableOvh: Boolean,
    onToggleOvh: (Boolean) -> Unit,
    ovhModel: String,
    onSelectOvhModel: (String) -> Unit,
    onTestPollinations: suspend (String) -> EmochiRepository.ProviderTestResult,
    onTestLlm7: suspend () -> EmochiRepository.ProviderTestResult,
    onTestOvh: suspend (String) -> EmochiRepository.ProviderTestResult,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var testingId by remember { mutableStateOf<String?>(null) }
    var testResults by remember { mutableStateOf<Map<String, EmochiRepository.ProviderTestResult>>(emptyMap()) }
    val llm7Stats = remember { Llm7RateLimitTracker.getUsageStats() }

    FullScreenMorCamSheetContainer(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlobalGlassIconOrb(
                    rootSize = rootSize,
                    blurredBg = blurredBg,
                    onClick = onDismiss,
                    modifier = Modifier.testTag("free_providers_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Geri",
                        tint = GlobalMorCamColors.PearlText,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Free API'siz Sağlayıcılar",
                        color = GlobalMorCamColors.PearlText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DisplaySerifFontFamily
                    )
                    Text(
                        text = if (prioritizeFreeKeyless)
                            "Öncelik modu AÇIK — etkin ücretsiz servisler ilk sırada denenir."
                        else
                            "Öncelik modu KAPALI — ekli API'lerinden sonra yedek olarak çalışır.",
                        color = GlobalMorCamColors.LavenderSubtext,
                        fontSize = 11.5.sp,
                        fontFamily = InterFontFamily
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ApiProviderCatalog.freeKeylessProviders.forEach { spec ->
                    val isEnabled = when (spec.id) {
                        "pollinations" -> enablePollinations
                        "llm7" -> enableLlm7
                        "ovh" -> enableOvh
                        else -> false
                    }
                    val currentModel = when (spec.id) {
                        "pollinations" -> pollinationsModel
                        "ovh" -> ovhModel
                        else -> spec.defaultModel
                    }

                    GlobalGlassCard(
                        rootSize = rootSize,
                        blurredBg = blurredBg,
                        cornerRadius = 20.dp,
                        highlighted = isEnabled,
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("free_provider_card_${spec.id}")
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (spec.id == "pollinations") Icons.Default.AutoAwesome else Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = if (isEnabled) GlobalMorCamColors.SoftEmerald else GlobalMorCamColors.BrightViolet,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = spec.name,
                                            color = GlobalMorCamColors.PearlText,
                                            fontSize = 15.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = InterFontFamily
                                        )
                                    }
                                    Text(
                                        text = "${spec.badge} • ${spec.endpointHint}",
                                        color = if (isEnabled) GlobalMorCamColors.SoftEmerald else GlobalMorCamColors.MutedLavender,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = InterFontFamily
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                GlobalMorCamSwitch(
                                    checked = isEnabled,
                                    onCheckedChange = { checked ->
                                        when (spec.id) {
                                            "pollinations" -> onTogglePollinations(checked)
                                            "llm7" -> onToggleLlm7(checked)
                                            "ovh" -> onToggleOvh(checked)
                                        }
                                    },
                                    modifier = Modifier.testTag("free_provider_switch_${spec.id}")
                                )
                            }

                            // Description
                            Text(
                                text = spec.description,
                                color = GlobalMorCamColors.LavenderSubtext,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                fontFamily = InterFontFamily
                            )

                            // Model choices if more than 1 model
                            if (spec.models.size > 1) {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    spec.models.forEach { m ->
                                        val selected = currentModel == m
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (selected) Color(0xCC7C3AED) else Color(0x6615082A))
                                                .border(
                                                    1.dp,
                                                    if (selected) Color(0xFFE9D5FF) else Color(0x55B794F4),
                                                    RoundedCornerShape(10.dp)
                                                )
                                                .clickable {
                                                    if (spec.id == "pollinations") onSelectPollinationsModel(m)
                                                    if (spec.id == "ovh") onSelectOvhModel(m)
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = m,
                                                color = if (selected) GlobalMorCamColors.PearlText else GlobalMorCamColors.LavenderSubtext,
                                                fontSize = 11.sp,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                fontFamily = InterFontFamily
                                            )
                                        }
                                    }
                                }
                            }

                            // Compact LLM7 usage line
                            if (spec.id == "llm7") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0x55100622))
                                        .padding(horizontal = 10.dp, vertical = 7.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Kota: ${llm7Stats.requestsLastMinute}/10 dk • ${llm7Stats.requestsLastHour}/60 saat",
                                        color = GlobalMorCamColors.MutedLavender,
                                        fontSize = 10.5.sp,
                                        fontFamily = InterFontFamily
                                    )
                                    Text(
                                        text = "${llm7Stats.tokensUsed24h} token",
                                        color = GlobalMorCamColors.LavenderSubtext,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = InterFontFamily
                                    )
                                }
                            }

                            // Test row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                GlobalGlassSecondaryButton(
                                    text = "Bağlantıyı Test Et",
                                    loading = testingId == spec.id,
                                    enabled = testingId == null,
                                    onClick = {
                                        scope.launch {
                                            testingId = spec.id
                                            val res = when (spec.id) {
                                                "pollinations" -> onTestPollinations(pollinationsModel)
                                                "llm7" -> onTestLlm7()
                                                "ovh" -> onTestOvh(ovhModel)
                                                else -> EmochiRepository.ProviderTestResult(false, false, "Desteklenmiyor")
                                            }
                                            testResults = testResults + (spec.id to res)
                                            testingId = null
                                        }
                                    },
                                    modifier = Modifier.testTag("free_provider_test_${spec.id}")
                                )
                            }

                            testResults[spec.id]?.let { res ->
                                TestResultGlassBanner(res)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            GlobalGlassPrimaryButton(
                text = "Tamam",
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("free_providers_done_button")
            )
        }
    }
}

@Composable
internal fun TestResultGlassBanner(res: EmochiRepository.ProviderTestResult) {
    val bg = if (res.isSuccess) Color(0x3310B981) else Color(0x33EF4444)
    val border = if (res.isSuccess) Color(0x996EE7B7) else Color(0x99FDA4AF)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (res.isSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = if (res.isSuccess) GlobalMorCamColors.SoftEmerald else GlobalMorCamColors.SoftRose,
            modifier = Modifier.size(18.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (res.isSuccess) "Bağlantı Başarılı (${res.latencyMs} ms)" else "Bağlantı Hatası",
                color = GlobalMorCamColors.PearlText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = InterFontFamily
            )
            Text(
                text = if (res.isSuccess) {
                    if (res.supportsFunctionCalling) "Gelişmiş hafıza araçları destekleniyor."
                    else "Standart sohbet yanıtları hazır."
                } else res.errorMessage.ifBlank { "Sunucu yanıt vermedi." },
                color = GlobalMorCamColors.LavenderSubtext,
                fontSize = 11.sp,
                lineHeight = 14.5.sp,
                fontFamily = InterFontFamily,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
