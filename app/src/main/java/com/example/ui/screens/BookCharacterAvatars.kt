package com.example.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFF0A071C))
            .border(borderWidth, borderColor.copy(alpha = 0.85f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = directResId),
            contentDescription = key.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
