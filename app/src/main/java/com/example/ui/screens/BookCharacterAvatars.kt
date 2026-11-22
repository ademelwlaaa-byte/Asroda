package com.example.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.book.BookCharacterId
import java.util.Locale
import kotlin.math.roundToInt

enum class CharacterPortraitKey(val accentColor: Color, val displayName: String) {
    AIDEN(Color(0xFF38BDF8), "Aiden Blackwood"),
    STEVE(Color(0xFF60A5FA), "Steve Rogers"),
    NATASHA(Color(0xFFF472B6), "Natasha Romanoff"),
    WANDA(Color(0xFFC084FC), "Wanda Maximoff"),
    TONY(Color(0xFFFBBF24), "Tony Stark"),
    CLINT(Color(0xFF34D399), "Clint Barton"),
    BRUCE(Color(0xFF4ADE80), "Bruce Banner"),
    THOR(Color(0xFF38BDF8), "Thor Odinson"),
    FURY(Color(0xFF94A3B8), "Nick Fury"),
    VAELOR(Color(0xFFF43F5E), "Yüce Hakem Vaelor"),
    PIETRO(Color(0xFF7DD3FC), "Pietro Maximoff"),
    VISION(Color(0xFFEAB308), "Vision"),
    ULTRON(Color(0xFFEF4444), "Ultron Prime"),
    ZERO(Color(0xFF38BDF8), "Sıfır · Yara İzli Aiden"),
    STRANGE(Color(0xFFF59E0B), "Dr. Stephen Strange"),
    ARCHITECT(Color(0xFF60A5FA), "Asıl Mimar"),
    VOSS(Color(0xFFF87171), "Elias Voss · 6-B"),
    MIRA(Color(0xFF38BDF8), "Mira · 8-C");

    companion object {
        fun fromBookCharacter(id: BookCharacterId): CharacterPortraitKey = when (id) {
            BookCharacterId.STEVE -> STEVE
            BookCharacterId.NATASHA -> NATASHA
            BookCharacterId.WANDA -> WANDA
            BookCharacterId.TONY -> TONY
            BookCharacterId.CLINT -> CLINT
            BookCharacterId.BRUCE -> BRUCE
            BookCharacterId.THOR -> THOR
            BookCharacterId.FURY -> FURY
            BookCharacterId.VOSS -> VOSS
            BookCharacterId.MIRA -> MIRA
            BookCharacterId.PIETRO -> PIETRO
            BookCharacterId.ZERO -> ZERO
            BookCharacterId.VISION -> VISION
        }

        fun resolveFromLabel(rawLabel: String): CharacterPortraitKey? {
            val u = rawLabel.uppercase(Locale.ROOT)
            return when {
                "SIFIR" in u || "YARA İZLİ" in u || "TWIN" in u || "GELECEK AIDEN" in u -> ZERO
                "MİMAR" in u || "ARCHITECT" in u -> ARCHITECT
                "ULTRON" in u -> ULTRON
                "VISION" in u || "VİZYON" in u || "JARVIS" in u -> VISION
                "PIETRO" in u || "QUICKSILVER" in u -> PIETRO
                "STRANGE" in u || "SANCTUM" in u -> STRANGE
                "VOSS" in u || "ELIAS" in u || "6-B" in u -> VOSS
                "MIRA" in u || "MİRA" in u || "8-C" in u -> MIRA
                "NATASHA" in u || "ROMANOFF" in u || "WIDOW" in u -> NATASHA
                "TONY" in u || "STARK" in u || "IRON" in u -> TONY
                "STEVE" in u || "ROGERS" in u || "KAPTAN" in u -> STEVE
                "WANDA" in u || "MAXIMOFF" in u -> WANDA
                "BRUCE" in u || "BANNER" in u -> BRUCE
                "CLINT" in u || "BARTON" in u || "HAWKEYE" in u -> CLINT
                "THOR" in u || "ODINSON" in u || "ASGARD" in u -> THOR
                "FURY" in u -> FURY
                "VAELOR" in u || "SOLUK MÜHÜR" in u -> VAELOR
                "AIDEN" in u || "BLACKWOOD" in u -> AIDEN
                else -> null
            }
        }
    }
}

private data class SquareFaceCrop(
    @param:DrawableRes val resId: Int,
    val centerXFrac: Float,
    val centerYFrac: Float,
    val widthFrac: Float
)

/**
 * Live-action movie-still character portrait medallion used across:
 * - Story character dossier & relations modal
 * - Telltale consequence banners
 * - In-story COMMS / DOSSIER / MIND cards
 * - Live Avengers support callouts inside the 3D Boss Game
 */
@Composable
fun CinematicCharacterAvatar(
    key: CharacterPortraitKey,
    size: Dp = 46.dp,
    borderWidth: Dp = 1.4.dp,
    borderColor: Color = key.accentColor,
    modifier: Modifier = Modifier
) {
    val directResId: Int? = when (key) {
        CharacterPortraitKey.AIDEN -> R.drawable.img_aiden_portrait
        CharacterPortraitKey.ZERO -> R.drawable.img_postcredits_twin_aiden
        CharacterPortraitKey.STEVE -> R.drawable.img_char_steve
        CharacterPortraitKey.NATASHA -> R.drawable.img_char_natasha
        CharacterPortraitKey.WANDA -> R.drawable.img_char_wanda
        CharacterPortraitKey.BRUCE -> R.drawable.img_char_bruce
        CharacterPortraitKey.FURY -> R.drawable.img_char_fury
        CharacterPortraitKey.THOR -> R.drawable.img_char_thor
        CharacterPortraitKey.VAELOR -> R.drawable.img_char_vaelor
        else -> null
    }

    val croppedSource: SquareFaceCrop? = when (key) {
        CharacterPortraitKey.TONY -> SquareFaceCrop(R.drawable.img_ch1_c3_stark_faceoff, 0.205f, 0.42f, 0.34f)
        CharacterPortraitKey.CLINT -> SquareFaceCrop(R.drawable.img_ch1_c2_direct_rift, 0.855f, 0.43f, 0.31f)
        CharacterPortraitKey.VOSS -> SquareFaceCrop(R.drawable.img_ch5_voss_blizzard, 0.72f, 0.35f, 0.46f)
        else -> null
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFF0A071C))
            .border(borderWidth, borderColor.copy(alpha = 0.85f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when {
            directResId != null -> {
                Image(
                    painter = painterResource(id = directResId),
                    contentDescription = key.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            croppedSource != null -> {
                CroppedLiveActionFace(
                    crop = croppedSource,
                    modifier = Modifier.fillMaxSize()
                )
            }

            key == CharacterPortraitKey.THOR -> {
                // High-craft Asgardian Storm & Mjolnir Emblem
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = this.size.width
                    val h = this.size.height
                    val cx = w / 2f
                    val cy = h / 2f
                    drawRect(
                        Brush.radialGradient(
                            listOf(Color(0xFF1D4ED8), Color(0xFF0F172A), Color(0xFF030712)),
                            center = Offset(cx, cy),
                            radius = w * 0.65f
                        )
                    )
                    // Electric lightning arcs
                    val bolt = Path().apply {
                        moveTo(cx + w * 0.08f, h * 0.14f)
                        lineTo(cx - w * 0.12f, cy + h * 0.02f)
                        lineTo(cx + w * 0.04f, cy + h * 0.02f)
                        lineTo(cx - w * 0.08f, h * 0.86f)
                    }
                    drawPath(bolt, color = Color(0xFF38BDF8), style = Stroke(width = w * 0.14f, cap = StrokeCap.Round))
                    drawPath(bolt, color = Color.White, style = Stroke(width = w * 0.055f, cap = StrokeCap.Round))
                }
            }

            key == CharacterPortraitKey.PIETRO -> {
                // Quicksilver Silver-Cyan Sonic Streak Medallion
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = this.size.width; val h = this.size.height; val cx = w / 2f; val cy = h / 2f
                    drawRect(Brush.radialGradient(listOf(Color(0xFF0C4A6E), Color(0xFF0F172A), Color(0xFF020617)), center = Offset(cx, cy), radius = w * 0.7f))
                    val streak1 = Path().apply {
                        moveTo(w * 0.16f, h * 0.72f); lineTo(w * 0.56f, h * 0.24f); lineTo(w * 0.48f, h * 0.50f); lineTo(w * 0.84f, h * 0.30f)
                    }
                    drawPath(streak1, color = Color(0xFF38BDF8), style = Stroke(width = w * 0.12f, cap = StrokeCap.Round))
                    drawPath(streak1, color = Color(0xFFE0F2FE), style = Stroke(width = w * 0.045f, cap = StrokeCap.Round))
                }
            }

            key == CharacterPortraitKey.VISION -> {
                // Vision Mind-Stone Golden Diamond + Crimson Synthoid Brow Medallion
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = this.size.width; val h = this.size.height; val cx = w / 2f; val cy = h / 2f
                    drawRect(Brush.radialGradient(listOf(Color(0xFF7F1D1D), Color(0xFF1E1B4B), Color(0xFF050814)), center = Offset(cx, cy), radius = w * 0.7f))
                    val stone = Path().apply {
                        moveTo(cx, h * 0.16f); lineTo(w * 0.72f, cy); lineTo(cx, h * 0.84f); lineTo(w * 0.28f, cy); close()
                    }
                    drawPath(stone, brush = Brush.radialGradient(listOf(Color(0xFFFEF9C3), Color(0xFFEAB308), Color(0xFFB45309)), center = Offset(cx, cy), radius = w * 0.36f))
                    drawPath(stone, color = Color(0xFFFEF08A), style = Stroke(width = w * 0.045f))
                }
            }

            key == CharacterPortraitKey.ULTRON -> {
                // Ultron Prime Vibranium Crimson Visor Medallion
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = this.size.width; val h = this.size.height; val cx = w / 2f; val cy = h / 2f
                    drawRect(Brush.radialGradient(listOf(Color(0xFF450A0A), Color(0xFF18181B), Color(0xFF030306)), center = Offset(cx, cy), radius = w * 0.7f))
                    val visor = Path().apply {
                        moveTo(w * 0.18f, h * 0.42f); lineTo(w * 0.42f, h * 0.48f); lineTo(w * 0.34f, h * 0.56f); close()
                        moveTo(w * 0.82f, h * 0.42f); lineTo(w * 0.58f, h * 0.48f); lineTo(w * 0.66f, h * 0.56f); close()
                    }
                    drawPath(visor, color = Color(0xFFEF4444))
                    val jaw = Path().apply {
                        moveTo(w * 0.26f, h * 0.66f); lineTo(cx, h * 0.78f); lineTo(w * 0.74f, h * 0.66f)
                    }
                    drawPath(jaw, color = Color(0xFFFCA5A5), style = Stroke(width = w * 0.06f, cap = StrokeCap.Round))
                    drawCircle(color = Color(0x66EF4444), radius = w * 0.34f, center = Offset(cx, cy), style = Stroke(width = w * 0.035f))
                }
            }

            key == CharacterPortraitKey.STRANGE -> {
                // Doctor Strange Sanctum Golden Mandala Seal
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = this.size.width; val h = this.size.height; val cx = w / 2f; val cy = h / 2f
                    drawRect(Brush.radialGradient(listOf(Color(0xFF78350F), Color(0xFF1C1917), Color(0xFF09070B)), center = Offset(cx, cy), radius = w * 0.7f))
                    drawCircle(color = Color(0xFFF59E0B), radius = w * 0.36f, center = Offset(cx, cy), style = Stroke(width = w * 0.05f))
                    val tri = Path().apply {
                        moveTo(cx, h * 0.18f); lineTo(w * 0.78f, h * 0.70f); lineTo(w * 0.22f, h * 0.70f); close()
                    }
                    drawPath(tri, color = Color(0xFFFDE047), style = Stroke(width = w * 0.045f))
                    drawCircle(color = Color(0xFF34D399), radius = w * 0.11f, center = Offset(cx, cy + h * 0.03f))
                }
            }

            key == CharacterPortraitKey.ARCHITECT -> {
                // Asıl Mimar — Golden-Blue Single-Line Singularity Eye
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = this.size.width; val h = this.size.height; val cx = w / 2f; val cy = h / 2f
                    drawRect(Brush.radialGradient(listOf(Color(0xFF1E3A8A), Color(0xFF090B1A), Color(0xFF010207)), center = Offset(cx, cy), radius = w * 0.72f))
                    drawLine(Color(0xFFEAB308), Offset(w * 0.12f, cy), Offset(w * 0.88f, cy), strokeWidth = w * 0.055f, cap = StrokeCap.Round)
                    drawCircle(Brush.radialGradient(listOf(Color.White, Color(0xFF38BDF8), Color.Transparent)), radius = w * 0.28f, center = Offset(cx, cy))
                    drawCircle(Color(0xFFFEF08A), radius = w * 0.34f, center = Offset(cx, cy), style = Stroke(width = w * 0.04f))
                }
            }

            key == CharacterPortraitKey.MIRA -> {
                // Mira (Denek 8-C) — Harmonized Dual Blue-Crimson Resonance Crest
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = this.size.width; val h = this.size.height; val cx = w / 2f; val cy = h / 2f
                    drawRect(Brush.radialGradient(listOf(Color(0xFF4C0519), Color(0xFF0C4A6E), Color(0xFF030712)), center = Offset(cx, cy), radius = w * 0.72f))
                    drawCircle(color = Color(0xFF38BDF8), radius = w * 0.34f, center = Offset(cx, cy), style = Stroke(width = w * 0.055f))
                    drawCircle(color = Color(0xFFF43F5E), radius = w * 0.22f, center = Offset(cx, cy), style = Stroke(width = w * 0.05f))
                    drawCircle(Brush.radialGradient(listOf(Color.White, Color(0xFFBAE6FD), Color.Transparent)), radius = w * 0.16f, center = Offset(cx, cy))
                }
            }

            else -> {
                // Vaelor Pale Veil Cosmic Arbiter Emblem
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = this.size.width
                    val h = this.size.height
                    val cx = w / 2f
                    val cy = h / 2f
                    drawRect(
                        Brush.radialGradient(
                            listOf(Color(0xFF4C0519), Color(0xFF0F172A), Color(0xFF020617)),
                            center = Offset(cx, cy),
                            radius = w * 0.68f
                        )
                    )
                    // Silver cathedral crown chevron
                    val crown = Path().apply {
                        moveTo(cx, h * 0.16f)
                        lineTo(w * 0.78f, cy)
                        lineTo(cx, h * 0.84f)
                        lineTo(w * 0.22f, cy)
                        close()
                    }
                    drawPath(crown, color = Color(0xFF1E293B))
                    drawPath(crown, color = Color(0xFFCBD5E1), style = Stroke(width = w * 0.05f))
                    drawCircle(
                        brush = Brush.radialGradient(listOf(Color.White, Color(0xFFF43F5E), Color.Transparent)),
                        radius = w * 0.22f,
                        center = Offset(cx, cy)
                    )
                }
            }
        }
    }
}

@Composable
private fun CroppedLiveActionFace(
    crop: SquareFaceCrop,
    modifier: Modifier = Modifier
) {
    val bmp: ImageBitmap = ImageBitmap.imageResource(id = crop.resId)
    val srcRect = remember(bmp, crop) {
        val side = (bmp.width * crop.widthFrac).roundToInt().coerceIn(32, minOf(bmp.width, bmp.height))
        val left = (bmp.width * crop.centerXFrac - side / 2f).roundToInt().coerceIn(0, bmp.width - side)
        val top = (bmp.height * crop.centerYFrac - side / 2f).roundToInt().coerceIn(0, bmp.height - side)
        Pair(IntOffset(left, top), IntSize(side, side))
    }

    Canvas(modifier = modifier) {
        drawImage(
            image = bmp,
            srcOffset = srcRect.first,
            srcSize = srcRect.second,
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt())
        )
    }
}
