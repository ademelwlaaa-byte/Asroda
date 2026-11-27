package com.example.ui.screens

import androidx.annotation.DrawableRes
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.book.BookCharacterId
import java.util.Locale

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
            if (rawLabel.isBlank()) return null
            val u = rawLabel.uppercase(Locale.ROOT)
            val uTr = rawLabel.uppercase(Locale.forLanguageTag("tr-TR"))
            fun has(vararg tokens: String): Boolean = tokens.any { it in u || it in uTr }
            return when {
                has("SIFIR", "YARA İZLİ", "YARA IZLI", "TWIN", "GELECEK AIDEN") -> ZERO
                has("MİMAR", "MIMAR", "ARCHITECT") -> ARCHITECT
                has("ULTRON") -> ULTRON
                has("VISION", "VİZYON", "VIZYON", "JARVIS") -> VISION
                has("PIETRO", "PİETRO", "QUICKSILVER") -> PIETRO
                has("STRANGE", "SANCTUM") -> STRANGE
                has("VOSS", "ELIAS", "ELİAS", "6-B") -> VOSS
                has("MIRA", "MİRA", "8-C") -> MIRA
                has("NATASHA", "ROMANOFF", "WIDOW") -> NATASHA
                has("TONY", "STARK", "IRON") -> TONY
                has("STEVE", "ROGERS", "KAPTAN") -> STEVE
                has("WANDA", "MAXIMOFF") -> WANDA
                has("BRUCE", "BANNER") -> BRUCE
                has("CLINT", "CLİNT", "BARTON", "HAWKEYE") -> CLINT
                has("THOR", "ODINSON", "ODİNSON", "ASGARD") -> THOR
                has("FURY") -> FURY
                has("VAELOR", "SOLUK MÜHÜR", "SOLUK MUHUR") -> VAELOR
                has("AIDEN", "AİDEN", "BLACKWOOD") -> AIDEN
                else -> null
            }
        }
    }
}

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
    @DrawableRes val directResId: Int = when (key) {
        CharacterPortraitKey.AIDEN -> R.drawable.img_aiden_portrait
        CharacterPortraitKey.ZERO -> R.drawable.img_postcredits_twin_aiden
        CharacterPortraitKey.STEVE -> R.drawable.img_steve_rogers_v2_1795622788903
        CharacterPortraitKey.NATASHA -> R.drawable.img_natasha_v5_portrait_1795640601606
        CharacterPortraitKey.WANDA -> R.drawable.img_char_wanda
        CharacterPortraitKey.TONY -> R.drawable.img_tony_stark_v2_1795622790425
        CharacterPortraitKey.CLINT -> R.drawable.img_portrait_clint_hero_1795559319365
        CharacterPortraitKey.PIETRO -> R.drawable.img_portrait_pietro_hero_1795559251696
        CharacterPortraitKey.VISION -> R.drawable.img_portrait_vision_hero_1795559320364
        CharacterPortraitKey.STRANGE -> R.drawable.img_portrait_strange_hero_1795616988269
        CharacterPortraitKey.ARCHITECT -> R.drawable.img_portrait_architect_hero_1795559252160
        CharacterPortraitKey.MIRA -> R.drawable.img_portrait_mira_8c_1795557413439
        CharacterPortraitKey.VOSS -> R.drawable.img_portrait_voss_6b_1795557413860
        CharacterPortraitKey.ULTRON -> R.drawable.img_portrait_ultron_prime_1795557413989
        CharacterPortraitKey.BRUCE -> R.drawable.img_char_bruce
        CharacterPortraitKey.FURY -> R.drawable.img_char_fury
        CharacterPortraitKey.THOR -> R.drawable.img_char_thor
        CharacterPortraitKey.VAELOR -> R.drawable.img_char_vaelor
    }

    val context = LocalContext.current
    val avatarRequest = remember(context, directResId) {
        ImageRequest.Builder(context)
            .data(directResId)
            .allowRgb565(true)
            .crossfade(160)
            .build()
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFF0A071C))
            .border(borderWidth, borderColor.copy(alpha = 0.85f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = avatarRequest,
            contentDescription = key.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
