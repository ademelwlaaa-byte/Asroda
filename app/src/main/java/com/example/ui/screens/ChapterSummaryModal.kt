package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.components.verticalFadingEdge
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.R
import com.example.book.BookCharacterId
import com.example.book.BookReadState
import com.example.book.BookStateRepository
import com.example.book.CosmicDriftBookRegistry
import com.example.book.RecordedDecision
import com.example.data.local.MessageEntity
import com.example.ui.animation.LocalCosmicMotionConfig
import com.example.ui.animation.cosmicStaggeredEnter
import com.example.ui.animation.cosmicTapSpring
import com.example.ui.components.MorCamButtonVariant
import com.example.ui.components.MorCamCapsuleButton
import com.example.ui.components.MorCamDivider
import com.example.ui.components.MorCamTokens
import com.example.ui.components.morCamCosmicGlow
import com.example.ui.theme.DisplaySerifFontFamily
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LiterarySerifFontFamily
import com.example.util.BookAudioController
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

private data class MeaningfulImpactLine(
    val text: String,
    val tint: Color,
    val symbol: String,
    val isNewFlag: Boolean = false
)

private enum class ChoiceAtmosphereMode {
    RAIN_NEON,
    WHITE_EYE,
    RED_ALARM,
    BLIZZARD_SNOW,
    ISTANBUL_EMBER,
    COSMIC_STORM,
    GOLDEN_DAWN
}

private data class ChoiceVisualSpec(
    @param:DrawableRes val artRes: Int,
    val atmosphere: ChoiceAtmosphereMode,
    val moodTag: String
)

private data class Lis2DecisionSlide(
    val index: Int,
    val nodeId: String,
    val title: String,
    val selectedOption: String,
    val impactText: String,
    val isBranching: Boolean,
    val isRelationship: Boolean,
    val secretUnlocked: String?,
    val branchTag: String?,
    @param:DrawableRes val artworkResId: Int,
    val atmosphere: ChoiceAtmosphereMode,
    val moodTag: String,
    val meaningfulImpacts: List<MeaningfulImpactLine>
)

/**
 * Resolves exact event- and choice-specific 2.5D live-action cinema artwork & atmospheric VFX
 * across ALL 10 chapters of Season 1 (every single decision and branch gets its own matched visual!).
 */
private fun resolveChapterChoiceVisual(
    chapterNumber: Int,
    choiceId: String,
    nodeId: String,
    orderIndex: Int
): ChoiceVisualSpec = when (choiceId) {
    // ── BÖLÜM 1: KOZMİK SÜRÜKLENİŞ ──────────────────────────────────────────
    "ch1_c1_direct" -> ChoiceVisualSpec(R.drawable.img_ch1_c2_direct_rift, ChoiceAtmosphereMode.COSMIC_STORM, "MAVİ KIVILCIMLA GİRİŞ")
    "ch1_c1_shadow" -> ChoiceVisualSpec(R.drawable.img_ch1_c2_stealth_terrace, ChoiceAtmosphereMode.RAIN_NEON, "GÖLGEDEN GİRİŞ")
    "ch1_c1_echo" -> ChoiceVisualSpec(R.drawable.img_ch1_c4_wanda_gaze, ChoiceAtmosphereMode.ISTANBUL_EMBER, "KIZIL İPLİK YANKISI")
    "ch1_c2_stay_terms" -> ChoiceVisualSpec(R.drawable.img_ch1_c3_stark_faceoff, ChoiceAtmosphereMode.RAIN_NEON, "EŞİT ŞARTLAR")
    "ch1_c2_probe_mind" -> ChoiceVisualSpec(R.drawable.img_ch1_c1_vision_trace, ChoiceAtmosphereMode.WHITE_EYE, "BEYAZ GÖZ YOKLAMASI")
    "ch1_c2_cold_trial" -> ChoiceVisualSpec(R.drawable.img_ch1_c5_tower_night, ChoiceAtmosphereMode.RAIN_NEON, "TEK GECELİK MESAFE")

    // ── BÖLÜM 2: ŞARTLAR ────────────────────────────────────────────────────
    "ch2_c1_measured" -> ChoiceVisualSpec(R.drawable.img_ch1_c3_stark_faceoff, ChoiceAtmosphereMode.COSMIC_STORM, "ASKIDA KALAN ZAMAN")
    "ch2_c1_white_eye" -> ChoiceVisualSpec(R.drawable.img_ch1_c1_vision_trace, ChoiceAtmosphereMode.WHITE_EYE, "ÜÇ SANİYELİK BOŞLUK")
    "ch2_c1_refuse" -> ChoiceVisualSpec(R.drawable.img_ch1_c3_stark_faceoff, ChoiceAtmosphereMode.RAIN_NEON, "TAVİZSİZ SINIR")
    "ch2_c2_natasha" -> ChoiceVisualSpec(R.drawable.img_scene_natasha_aiden_terrace_1795640635755, ChoiceAtmosphereMode.RAIN_NEON, "TERASTA YAN YANA · NATASHA & AIDEN")
    "ch2_c2_wanda" -> ChoiceVisualSpec(R.drawable.img_end_wanda_aiden_pier_1795553970888, ChoiceAtmosphereMode.ISTANBUL_EMBER, "KIZIL DENGE · WANDA & AIDEN")
    "ch2_c2_alone" -> ChoiceVisualSpec(R.drawable.img_ch1_c5_tower_night, ChoiceAtmosphereMode.RAIN_NEON, "BİLİNÇLİ MESAFE")

    // ── BÖLÜM 3: DENEME SÜRESİ ──────────────────────────────────────────────
    "ch3_c1_honor" -> ChoiceVisualSpec(R.drawable.img_char_steve, ChoiceAtmosphereMode.GOLDEN_DAWN, "ONURLU DÜELLO · STEVE")
    "ch3_c1_cosmic" -> ChoiceVisualSpec(R.drawable.img_ch1_c2_direct_rift, ChoiceAtmosphereMode.COSMIC_STORM, "KOZMİK FIRTINA")
    "ch3_c1_tactical" -> ChoiceVisualSpec(R.drawable.img_natasha_v5_portrait_1795640601606, ChoiceAtmosphereMode.RAIN_NEON, "SESSİZ DENGE")
    "ch3_c2_stand_ground" -> ChoiceVisualSpec(R.drawable.img_ch1_c3_stark_faceoff, ChoiceAtmosphereMode.RED_ALARM, "KIRMIZI ALARM · KALKAN")
    "ch3_c2_white_eye_lock" -> ChoiceVisualSpec(R.drawable.img_ch1_c1_vision_trace, ChoiceAtmosphereMode.WHITE_EYE, "BEYAZ GÖZ KİLİTLENMESİ")
    "ch3_c2_vanish_return" -> ChoiceVisualSpec(R.drawable.img_ch1_c1_vision_calm, ChoiceAtmosphereMode.RAIN_NEON, "ÇATIDA HESAPLAŞMA")

    // ── BÖLÜM 4: İLK GÖREV (19 NUMARALI RIHTIM) ────────────────────────────
    "ch4_c1_shield_team" -> ChoiceVisualSpec(R.drawable.img_ch1_c2_forced_rift, ChoiceAtmosphereMode.RAIN_NEON, "RIHTIMDA EKİP KALKANI")
    "ch4_c1_shadow_natasha" -> ChoiceVisualSpec(R.drawable.natasha_3d_turned_1795640635973, ChoiceAtmosphereMode.RAIN_NEON, "GÖLGE İKİLİSİ · NATASHA")
    "ch4_c1_solo_overwhelm" -> ChoiceVisualSpec(R.drawable.img_ch1_c1_vision_trace, ChoiceAtmosphereMode.WHITE_EYE, "TEK KİŞİLİK FIRTINA")
    "ch4_c2_share_all" -> ChoiceVisualSpec(R.drawable.img_ch1_c3_stark_faceoff, ChoiceAtmosphereMode.GOLDEN_DAWN, "AÇIK DOSYA · DENEK 7-A")
    "ch4_c2_hide_disk" -> ChoiceVisualSpec(R.drawable.img_ch1_c1_vision_calm, ChoiceAtmosphereMode.RAIN_NEON, "SAKLI VERİ DİSKİ")

    // ── BÖLÜM 5: KIRMIZI BUZ (ELIAS VOSS) ───────────────────────────────────
    "ch5_c1_capture" -> ChoiceVisualSpec(R.drawable.img_ch5_voss_blizzard, ChoiceAtmosphereMode.BLIZZARD_SNOW, "BROOKLYN KAR FIRTINASI")
    "ch5_c1_talk_alone" -> ChoiceVisualSpec(R.drawable.img_ch5_voss_blizzard, ChoiceAtmosphereMode.WHITE_EYE, "KARDA ZİHİN İTİRAFI")
    "ch5_c2_protect_voss" -> ChoiceVisualSpec(R.drawable.img_ch1_c4_wanda_gaze, ChoiceAtmosphereMode.GOLDEN_DAWN, "KAFES KIRILDI · VOSS")
    "ch5_c2_handover_voss" -> ChoiceVisualSpec(R.drawable.img_ch5_voss_blizzard, ChoiceAtmosphereMode.RED_ALARM, "SOĞUKKANLI PROTOKOL")

    // ── BÖLÜM 6: KIRIK AYNA (İSTANBUL ARŞİVİ) ──────────────────────────────
    "ch6_c1_natasha" -> ChoiceVisualSpec(R.drawable.img_ch6_istanbul_cistern, ChoiceAtmosphereMode.ISTANBUL_EMBER, "İSTANBUL SARNICI · NATASHA")
    "ch6_c1_wanda" -> ChoiceVisualSpec(R.drawable.img_ch1_c4_wanda_gaze, ChoiceAtmosphereMode.ISTANBUL_EMBER, "MAVİ VE KIZIL REZONANS")
    "ch6_c1_solo" -> ChoiceVisualSpec(R.drawable.img_ch6_istanbul_cistern, ChoiceAtmosphereMode.COSMIC_STORM, "YEREBATAN YALNIZ YÜZLEŞME")
    "ch6_c2_stay_and_step" -> ChoiceVisualSpec(R.drawable.img_ch6_istanbul_cistern, ChoiceAtmosphereMode.GOLDEN_DAWN, "BOĞAZ KIYISINDA İLK ADIM")
    "ch6_c2_finish_first" -> ChoiceVisualSpec(R.drawable.img_ch1_c5_tower_night, ChoiceAtmosphereMode.RAIN_NEON, "ÖNCE HESAPLAŞMA")

    // ── BÖLÜM 7: KARANLIK AYNA (MİRA & ŞAFAK) ──────────────────────────────
    "ch7_c1_team_plan" -> ChoiceVisualSpec(R.drawable.img_ch6_istanbul_cistern, ChoiceAtmosphereMode.ISTANBUL_EMBER, "AVENGERS İLE FİNAL PLANI")
    "ch7_c1_solo_plan" -> ChoiceVisualSpec(R.drawable.img_anim_bosphorus, ChoiceAtmosphereMode.COSMIC_STORM, "TÜNELLERDE YALNIZ YÜRÜYÜŞ")
    "ch7_c2_save_mira" -> ChoiceVisualSpec(R.drawable.img_anim_bosphorus, ChoiceAtmosphereMode.ISTANBUL_EMBER, "MİRA KURTARILDI")
    "ch7_c2_destroy_tower" -> ChoiceVisualSpec(R.drawable.img_anim_bosphorus, ChoiceAtmosphereMode.COSMIC_STORM, "REZONANS KULESİ YIKILDI")
    "ch7_c3_natasha_home" -> ChoiceVisualSpec(R.drawable.img_scene_natasha_aiden_terrace_1795640635755, ChoiceAtmosphereMode.GOLDEN_DAWN, "BOĞAZ'DA ŞAFAK · NATASHA & AIDEN")
    "ch7_c3_wanda_home" -> ChoiceVisualSpec(R.drawable.img_end_wanda_aiden_pier_1795553970888, ChoiceAtmosphereMode.GOLDEN_DAWN, "BOĞAZ'DA ŞAFAK · WANDA & AIDEN")
    "ch7_c3_lone_horizon" -> ChoiceVisualSpec(R.drawable.img_ch1_c1_vision_calm, ChoiceAtmosphereMode.GOLDEN_DAWN, "ÖZGÜR UFUK")

    // ── BÖLÜM 8: KIRIK GÖKLER (THOR & KAELEN) ──────────────────────────────
    "ch8_c1_thor_sky" -> ChoiceVisualSpec(R.drawable.img_anim_armada, ChoiceAtmosphereMode.COSMIC_STORM, "GÖK BARİYERİ · THOR")
    "ch8_c1_natasha_bunker" -> ChoiceVisualSpec(R.drawable.img_natasha_v5_portrait_1795640601606, ChoiceAtmosphereMode.RED_ALARM, "KONSEY SIĞINAĞI BASKINI")
    "ch8_c1_white_eye_trace" -> ChoiceVisualSpec(R.drawable.img_ch1_c1_vision_trace, ChoiceAtmosphereMode.WHITE_EYE, "KOZMİK ZİHİN HARİTASI")
    "ch8_c2_time_break" -> ChoiceVisualSpec(R.drawable.img_ch1_c2_direct_rift, ChoiceAtmosphereMode.COSMIC_STORM, "DONMUŞ SANİYE · KAELEN")
    "ch8_c2_absorb_reflect" -> ChoiceVisualSpec(R.drawable.img_anim_mutlak_uyanis, ChoiceAtmosphereMode.COSMIC_STORM, "KOZMİK YANKI PATLAMASI")
    "ch8_c2_white_eye_steal" -> ChoiceVisualSpec(R.drawable.img_ch1_c1_vision_trace, ChoiceAtmosphereMode.WHITE_EYE, "KAELEN ZİHİN SIZMASI")
    "ch8_c3_natasha" -> ChoiceVisualSpec(R.drawable.natasha_3d_turned_1795640635973, ChoiceAtmosphereMode.RAIN_NEON, "SAVAŞ ÖNCESİ · NATASHA")
    "ch8_c3_wanda" -> ChoiceVisualSpec(R.drawable.img_ch1_c4_wanda_gaze, ChoiceAtmosphereMode.ISTANBUL_EMBER, "SAVAŞ ÖNCESİ · WANDA")
    "ch8_c3_thor" -> ChoiceVisualSpec(R.drawable.img_char_thor, ChoiceAtmosphereMode.COSMIC_STORM, "ASGARD YILDIZ HARİTASI")
    "ch8_c3_alone" -> ChoiceVisualSpec(R.drawable.img_ch1_c5_tower_night, ChoiceAtmosphereMode.RAIN_NEON, "YALNIZ HAZIRLIK")
    "ch8_c4_arctic" -> ChoiceVisualSpec(R.drawable.img_anim_blue_sky_2006, ChoiceAtmosphereMode.BLIZZARD_SNOW, "KUZEY KUTBU CEPHESİ")
    "ch8_c4_orbit" -> ChoiceVisualSpec(R.drawable.img_anim_armada, ChoiceAtmosphereMode.COSMIC_STORM, "YÖRÜNGE TAARRUZU")

    // ── BÖLÜM 9: KIYAMET AYNASI (KUZEY KUTBU & VAELOR) ─────────────────────
    "ch9_c1_shatter", "ch9_c1_time_sabotage" -> ChoiceVisualSpec(R.drawable.img_anim_armada, ChoiceAtmosphereMode.BLIZZARD_SNOW, "SIFIR NOKTASI BASKINI")
    "ch9_c1_shadow_flank" -> ChoiceVisualSpec(R.drawable.img_natasha_v5_portrait_1795640601606, ChoiceAtmosphereMode.BLIZZARD_SNOW, "GÖLGE KUŞATMASI · VANCE")
    "ch9_c2_cut_feed" -> ChoiceVisualSpec(R.drawable.img_ch1_c5_tower_night, ChoiceAtmosphereMode.RED_ALARM, "GÖLGEDE KALDIN")
    "ch9_c2_open_world" -> ChoiceVisualSpec(R.drawable.img_anim_blue_sky_2006, ChoiceAtmosphereMode.COSMIC_STORM, "SEKİZ MİLYAR TANIK")
    "ch9_c2_white_eye_turn" -> ChoiceVisualSpec(R.drawable.img_ch1_c1_vision_trace, ChoiceAtmosphereMode.WHITE_EYE, "VANCE'İN CANLI İTİRAFI")
    "ch9_c3_solo_tank", "ch9_c3_trinity", "ch9_c3_dimension_strike" -> ChoiceVisualSpec(R.drawable.img_game_rooftop_arena, ChoiceAtmosphereMode.COSMIC_STORM, "VAELOR İLK ÇARPIŞMA")
    "ch9_c4_natasha_vow" -> ChoiceVisualSpec(R.drawable.natasha_3d_turned_1795640635973, ChoiceAtmosphereMode.RAIN_NEON, "SON GECE · NATASHA'YA SÖZ")
    "ch9_c4_wanda_vow" -> ChoiceVisualSpec(R.drawable.img_ch1_c4_wanda_gaze, ChoiceAtmosphereMode.ISTANBUL_EMBER, "SON GECE · WANDA'YA SÖZ")
    "ch9_c4_alone_vow" -> ChoiceVisualSpec(R.drawable.img_ch1_c5_tower_night, ChoiceAtmosphereMode.RAIN_NEON, "HELICARRIER YALNIZ NÖBET")

    // ── BÖLÜM 10: YILDIZLARIN KIRILDIĞI GECE (SEZON FİNALİ) ────────────────
    "ch10_c1_lead_avengers" -> ChoiceVisualSpec(R.drawable.img_anim_armada, ChoiceAtmosphereMode.COSMIC_STORM, "MANHATTAN SAVAŞI · KOMUTAN")
    "ch10_c1_natasha_tower" -> ChoiceVisualSpec(R.drawable.img_natasha_v5_portrait_1795640601606, ChoiceAtmosphereMode.RED_ALARM, "KULE ÇEKİRDEĞİ BASKINI")
    "ch10_c1_solo_meteor" -> ChoiceVisualSpec(R.drawable.img_anim_mutlak_uyanis, ChoiceAtmosphereMode.COSMIC_STORM, "MAVİ METEOR TAARRUZU")
    "ch10_c2_justice", "ch10_c2_shatter_empire" -> ChoiceVisualSpec(R.drawable.img_ch1_c3_stark_faceoff, ChoiceAtmosphereMode.RED_ALARM, "KAFES PROTOKOLÜ ÇÖKTÜ")
    "ch10_c2_white_eye_erase" -> ChoiceVisualSpec(R.drawable.img_ch1_c1_vision_trace, ChoiceAtmosphereMode.WHITE_EYE, "BEYAZ GÖZ HÜKMÜ")
    "ch10_c3_branch_ship", "ch10_c4a_steal_crystal", "ch10_c4a_shatter_core" -> ChoiceVisualSpec(R.drawable.img_anim_armada, ChoiceAtmosphereMode.COSMIC_STORM, "ANA GEMİ ÇEKİRDEĞİ")
    "ch10_c3_branch_manhattan", "ch10_c4b_wanda_seal" -> ChoiceVisualSpec(R.drawable.img_anim_blue_sky_2006, ChoiceAtmosphereMode.COSMIC_STORM, "14 KASIM 2006 HAYALETİ")
    "ch10_c4b_white_eye_shatter" -> ChoiceVisualSpec(R.drawable.img_ch1_c1_vision_trace, ChoiceAtmosphereMode.WHITE_EYE, "İLLÜZYON KIRAN")
    "ch10_c5_time_blink", "ch10_c5_white_eye_fear", "ch10_c5_avengers_combo" -> ChoiceVisualSpec(R.drawable.img_game_rooftop_arena, ChoiceAtmosphereMode.COSMIC_STORM, "AVENGERS KULESİ ÇATI DÜELLOSU")
    "ch10_c6_shatter_banish", "ch10_c6_absorb_seal", "ch10_c6_rewind_city" -> ChoiceVisualSpec(R.drawable.img_anim_mutlak_uyanis, ChoiceAtmosphereMode.COSMIC_STORM, "MUTLAK UYANIŞ · 60 SANİYE")
    "ch10_c7_vanguard" -> ChoiceVisualSpec(R.drawable.img_ch1_c3_stark_faceoff, ChoiceAtmosphereMode.GOLDEN_DAWN, "FİNAL A · KOZMİK KOMUTAN")
    "ch10_c7_twin_stars" -> ChoiceVisualSpec(R.drawable.img_scene_natasha_aiden_terrace_1795640635755, ChoiceAtmosphereMode.GOLDEN_DAWN, "FİNAL B · GÖLGE VE IŞIK · YAN YANA")
    "ch10_c7_shield_reborn" -> ChoiceVisualSpec(R.drawable.img_ch1_c5_tower_night, ChoiceAtmosphereMode.GOLDEN_DAWN, "FİNAL C · KOZMİK NÖBET")
    "ch10_c8_open_s2", "ch10_c8_seal_s2" -> ChoiceVisualSpec(R.drawable.img_postcredits_twin_aiden, ChoiceAtmosphereMode.COSMIC_STORM, "2. SEZON · KIRIK ZAMAN SİNYALİ")

    // ── BÖLÜM 11: ESKİ DOSTLAR, YENİ GECE (2. SEZON AÇILIŞI) ───────────────
    "ch11_c1_share_unease", "ch11_c1_keep_stoic" -> ChoiceVisualSpec(R.drawable.img_ch1_c5_tower_night, ChoiceAtmosphereMode.GOLDEN_DAWN, "2. SEZON AÇILIŞI · KULE")
    "ch11_c2_wanda_comfort" -> ChoiceVisualSpec(R.drawable.img_ch1_c4_wanda_gaze, ChoiceAtmosphereMode.BLIZZARD_SNOW, "SOKOVİA KARI · WANDA")
    "ch11_c2_clint_shield" -> ChoiceVisualSpec(R.drawable.img_portrait_clint_hero_1795559319365, ChoiceAtmosphereMode.BLIZZARD_SNOW, "SOKOVİA · CLİNT KURTARIŞI")
    "ch11_c2_tony_scepter" -> ChoiceVisualSpec(R.drawable.img_portrait_tony_hero_1795559343343, ChoiceAtmosphereMode.COSMIC_STORM, "LOKİ'NİN ASASI · MİMAR İZİ")
    "ch11_c3_mjolnir_pure" -> ChoiceVisualSpec(R.drawable.img_anim_mutlak_uyanis, ChoiceAtmosphereMode.GOLDEN_DAWN, "MJOLNİR İKİ MİLİMETRE OYNADI")
    "ch11_c3_mjolnir_cheat" -> ChoiceVisualSpec(R.drawable.img_ch1_c2_direct_rift, ChoiceAtmosphereMode.COSMIC_STORM, "MJOLNİR ŞİMŞEĞİ")
    "ch11_c3_mjolnir_heart" -> ChoiceVisualSpec(R.drawable.img_anim_blue_sky_2006, ChoiceAtmosphereMode.WHITE_EYE, "KADİM RÜNLERİN YANKISI")
    "ch11_c3_mjolnir_natasha" -> ChoiceVisualSpec(R.drawable.natasha_3d_turned_1795640635973, ChoiceAtmosphereMode.GOLDEN_DAWN, "NATASHA İLE ZARİF PAS")
    "ch11_c3_mjolnir_wanda" -> ChoiceVisualSpec(R.drawable.img_ch1_c4_wanda_gaze, ChoiceAtmosphereMode.ISTANBUL_EMBER, "WANDA İLE TERAS PASI")
    "ch11_c4_romance_natasha" -> ChoiceVisualSpec(R.drawable.img_scene_natasha_aiden_terrace_1795640635755, ChoiceAtmosphereMode.RAIN_NEON, "BALKON · NATASHA & AIDEN YAN YANA")
    "ch11_c4_romance_wanda" -> ChoiceVisualSpec(R.drawable.img_end_wanda_aiden_pier_1795553970888, ChoiceAtmosphereMode.ISTANBUL_EMBER, "YILDIZLI TERAS · WANDA & AIDEN")
    "ch11_c4_brotherhood_team" -> ChoiceVisualSpec(R.drawable.img_ch1_c3_stark_faceoff, ChoiceAtmosphereMode.GOLDEN_DAWN, "AVENGERS KARDEŞLİĞİ")
    "ch11_c5_intercept_ultron" -> ChoiceVisualSpec(R.drawable.img_portrait_ultron_prime_1795557413989, ChoiceAtmosphereMode.RED_ALARM, "ULTRON · MİMAR TOHUMU")
    "ch11_c5_protect_allies", "ch11_c5_chase_scepter" -> ChoiceVisualSpec(R.drawable.img_portrait_ultron_prime_1795557413989, ChoiceAtmosphereMode.RED_ALARM, "ULTRON PARTİ SALDIRISI")

    // ── BÖLÜM 12: ÇATLAYAN GÜVEN ───────────────────────────────────────────
    "ch12_c1_hold_tony_accountable", "ch12_c1_bridge_tony", "ch12_c1_focus_architect" -> ChoiceVisualSpec(R.drawable.img_portrait_tony_hero_1795559343343, ChoiceAtmosphereMode.RED_ALARM, "STARK İLE HESAPLAŞMA")
    "ch12_c2_promise_pietro", "ch12_c2_tactical_caution" -> ChoiceVisualSpec(R.drawable.img_portrait_pietro_hero_1795559251696, ChoiceAtmosphereMode.COSMIC_STORM, "PİETRO MAXİMOFF İZİ")
    "ch12_c3_stealth_flank" -> ChoiceVisualSpec(R.drawable.img_natasha_v5_portrait_1795640601606, ChoiceAtmosphereMode.RAIN_NEON, "AFRİKA GÖLGE SIZMASI")
    "ch12_c3_wanda_front", "ch12_c3_front_vanguard" -> ChoiceVisualSpec(R.drawable.img_ch1_c2_direct_rift, ChoiceAtmosphereMode.COSMIC_STORM, "AFRİKA HURDA TERSANESİ")
    "ch12_c4_pietro_mercy", "ch12_c4_pietro_knockback" -> ChoiceVisualSpec(R.drawable.img_portrait_pietro_hero_1795559251696, ChoiceAtmosphereMode.COSMIC_STORM, "GÜMÜŞ ŞİMŞEKLE ÇARPIŞMA")

    // ── BÖLÜM 13: DONMUŞ ZAMAN VE KÂBUSLAR ─────────────────────────────────
    "ch13_c1_defy_architect", "ch13_c1_question_2006", "ch13_c1_cold_silence" -> ChoiceVisualSpec(R.drawable.img_portrait_architect_hero_1795559252160, ChoiceAtmosphereMode.WHITE_EYE, "ASIL MİMAR İLE YÜZ YÜZE")
    "ch13_c2_wake_natasha" -> ChoiceVisualSpec(R.drawable.img_natasha_v5_portrait_1795640601606, ChoiceAtmosphereMode.WHITE_EYE, "NATASHA'YI KÂBUSTAN KURTARDIN")
    "ch13_c2_shield_wanda" -> ChoiceVisualSpec(R.drawable.img_ch1_c4_wanda_gaze, ChoiceAtmosphereMode.ISTANBUL_EMBER, "WANDA İÇİN FEDAKÂRLIK")
    "ch13_c2_wake_steve_thor" -> ChoiceVisualSpec(R.drawable.img_ch1_c2_direct_rift, ChoiceAtmosphereMode.COSMIC_STORM, "KAPTAN VE THOR UYANDI")
    "ch13_c3_reach_zero", "ch13_c3_defend_tony", "ch13_c3_stop_zero" -> ChoiceVisualSpec(R.drawable.img_postcredits_twin_aiden, ChoiceAtmosphereMode.COSMIC_STORM, "YARA İZLİ SIFIR'IN KURTARIŞI")
    "ch13_c4_sit_natasha" -> ChoiceVisualSpec(R.drawable.natasha_3d_turned_1795640635973, ChoiceAtmosphereMode.RAIN_NEON, "QUİNJET · NATASHA İLE SESSİZLİK")
    "ch13_c4_sit_wanda" -> ChoiceVisualSpec(R.drawable.img_ch1_c4_wanda_gaze, ChoiceAtmosphereMode.ISTANBUL_EMBER, "QUİNJET · WANDA'YA DESTEK")
    "ch13_c4_sit_bruce_tony" -> ChoiceVisualSpec(R.drawable.img_ch1_c3_stark_faceoff, ChoiceAtmosphereMode.RAIN_NEON, "EKİP MORALİ · DÖNÜŞ")

    // ── BÖLÜM 14: BARTON ÇİFTLİĞİ & İLK ÖPÜCÜK ────────────────────────────
    "ch14_c1_calm_both", "ch14_c1_side_steve", "ch14_c1_side_tony" -> ChoiceVisualSpec(R.drawable.img_ch1_c3_stark_faceoff, ChoiceAtmosphereMode.GOLDEN_DAWN, "ÇİFTLİK · STEVE VE TONY")
    "ch14_c2_tell_everything", "ch14_c2_partial_truth" -> ChoiceVisualSpec(R.drawable.img_char_fury, ChoiceAtmosphereMode.RAIN_NEON, "NİCK FURY · GÜVEN MASASI")
    "ch14_c3_kiss_natasha", "ch14_c4_nat_promise_kofte", "ch14_c4_nat_promise_stay" -> ChoiceVisualSpec(R.drawable.img_scene_natasha_aiden_terrace_1795640635755, ChoiceAtmosphereMode.RAIN_NEON, "♥ YAĞMURLU VERANDA · NATASHA & AIDEN")
    "ch14_c3_kiss_wanda", "ch14_c4_wanda_promise_pietro", "ch14_c4_wanda_promise_home" -> ChoiceVisualSpec(R.drawable.img_end_wanda_aiden_pier_1795553970888, ChoiceAtmosphereMode.GOLDEN_DAWN, "♥ YILDIZLI İSKELE · WANDA & AIDEN")
    "ch14_c3_brotherhood_hearth", "ch14_c4_bro_vow_all" -> ChoiceVisualSpec(R.drawable.img_portrait_clint_hero_1795559319365, ChoiceAtmosphereMode.GOLDEN_DAWN, "ŞÖMİNE BAŞI · ÇELİK KARDEŞLİK")

    // ── BÖLÜM 15: KIRIK DENEKLERİN SIĞINAĞI (VOSS & MİRA) ─────────────────
    "ch15v_c1_share_pain", "ch15v_c1_mentor_control" -> ChoiceVisualSpec(R.drawable.img_portrait_voss_6b_1795557413860, ChoiceAtmosphereMode.GOLDEN_DAWN, "BROOKLYN SIĞINAĞI · VOSS")
    "ch15v_c2_mira_future", "ch15v_c2_mira_power" -> ChoiceVisualSpec(R.drawable.img_portrait_mira_8c_1795557413439, ChoiceAtmosphereMode.GOLDEN_DAWN, "YENİ HAYAT · MİRA")
    "ch15v_c3_bring_them", "ch15v_c3_protect_home" -> ChoiceVisualSpec(R.drawable.img_anim_bosphorus, ChoiceAtmosphereMode.ISTANBUL_EMBER, "6-B, 7-A VE 8-C · AİLE")
    "ch15v_c4_family_vow" -> ChoiceVisualSpec(R.drawable.img_ch5_voss_blizzard, ChoiceAtmosphereMode.GOLDEN_DAWN, "KARDEŞLİK YEMİNİ")

    // ── BÖLÜM 16: SEUL BEŞİĞİ & AYNA DÜELLOSU ──────────────────────────────
    "ch15_c1_save_train_pietro", "ch15_c1_grab_natasha" -> ChoiceVisualSpec(R.drawable.img_portrait_pietro_hero_1795559251696, ChoiceAtmosphereMode.COSMIC_STORM, "SEUL OTOYOLU · PİETRO DÖNDÜ")
    "ch15_c2_challenge_hope", "ch15_c2_challenge_fury" -> ChoiceVisualSpec(R.drawable.img_postcredits_twin_aiden, ChoiceAtmosphereMode.COSMIC_STORM, "SIFIR KULE'Yİ BASTI")
    "ch15_c3_spare_zero", "ch15_c3_stun_zero" -> ChoiceVisualSpec(R.drawable.img_postcredits_twin_aiden, ChoiceAtmosphereMode.COSMIC_STORM, "AYNA DÜELLOSU · ZAFER")
    "ch15_c4_welcome_vision", "ch15_c4_guard_vision" -> ChoiceVisualSpec(R.drawable.img_portrait_vision_hero_1795559320364, ChoiceAtmosphereMode.GOLDEN_DAWN, "VİSİON'IN DOĞUŞU")

    // ── BÖLÜM 17: VİSİON'IN GÖZLERİ & MİMAR'IN SIRRI ───────────────────────
    "ch16_c1_trust_vision", "ch16_c1_ask_vision_zero" -> ChoiceVisualSpec(R.drawable.img_portrait_vision_hero_1795559320364, ChoiceAtmosphereMode.GOLDEN_DAWN, "VİSİON MJOLNİR'İ KALDIRDI")
    "ch16_c2_accept_destiny", "ch16_c2_ask_strange_help" -> ChoiceVisualSpec(R.drawable.img_portrait_strange_hero_1795616988269, ChoiceAtmosphereMode.COSMIC_STORM, "DR. STRANGE · TEKİLLİK GERÇEĞİ")
    "ch16_c3_brief_bruce_nat" -> ChoiceVisualSpec(R.drawable.img_natasha_v5_portrait_1795640601606, ChoiceAtmosphereMode.RED_ALARM, "NATASHA'NIN MORS SİNYALİ")
    "ch16_c3_brief_twins", "ch16_c3_brief_vision_tony" -> ChoiceVisualSpec(R.drawable.img_ch1_c5_tower_night, ChoiceAtmosphereMode.COSMIC_STORM, "SOKOVİA HAREKAT PLANI")
    "ch16_c4_enter_alone", "ch16_c4_enter_guarded" -> ChoiceVisualSpec(R.drawable.img_postcredits_twin_aiden, ChoiceAtmosphereMode.RAIN_NEON, "REVİR KAPISINDA İKİ AİDEN")

    // ── BÖLÜM 18: YARA İZİNİN GERÇEĞİ ──────────────────────────────────────
    "ch17_c1_trust_zero_ally", "ch17_c1_keep_zero_tower" -> ChoiceVisualSpec(R.drawable.img_postcredits_twin_aiden, ChoiceAtmosphereMode.COSMIC_STORM, "YARA İZİNİN İTİRAFI")
    "ch17_c2_natasha_with_bruce", "ch17_c3_bridge_civilians" -> ChoiceVisualSpec(R.drawable.img_natasha_v5_portrait_1795640601606, ChoiceAtmosphereMode.RED_ALARM, "NATASHA KAFESTEN KURTULDU")
    "ch17_c2_evac_with_twins", "ch17_c3_church_scout", "ch17_c4_rally_cry" -> ChoiceVisualSpec(R.drawable.img_anim_armada, ChoiceAtmosphereMode.RED_ALARM, "SOKOVİA YERDEN KOPUYOR")

    // ── BÖLÜM 19: GÖLGENİN KORUYUCULARI (VOSS & MİRA AVENGERS İLE) ─────────
    "ch19g_c1_stand_for_them", "ch19g_c1_bruce_vouch" -> ChoiceVisualSpec(R.drawable.img_portrait_voss_6b_1795557413860, ChoiceAtmosphereMode.GOLDEN_DAWN, "VOSS & MİRA AVENGERS MASASINDA")
    "ch19g_c2_talk_wanda_mira" -> ChoiceVisualSpec(R.drawable.img_portrait_mira_8c_1795557413439, ChoiceAtmosphereMode.ISTANBUL_EMBER, "WANDA & MİRA · PSİKOLOJİK BAĞ")
    "ch19g_c2_talk_bruce_vision" -> ChoiceVisualSpec(R.drawable.img_portrait_vision_hero_1795559320364, ChoiceAtmosphereMode.WHITE_EYE, "BRUCE & VİSİON · İNSANLIK")
    "ch19g_c2_talk_voss_pietro" -> ChoiceVisualSpec(R.drawable.img_portrait_pietro_hero_1795559251696, ChoiceAtmosphereMode.GOLDEN_DAWN, "VOSS & PİETRO · KARDEŞLİK")
    "ch19g_c3_ultron_debate", "ch19g_c3_architect_warning", "ch19g_c4_depart_sokovia" -> ChoiceVisualSpec(R.drawable.img_portrait_ultron_prime_1795557413989, ChoiceAtmosphereMode.RED_ALARM, "SOKOVİA ÖNCESİ SON GECE")

    // ── BÖLÜM 20: YÜKSELEN ŞEHİR ───────────────────────────────────────────
    "ch18_c1_help_wanda_plaza" -> ChoiceVisualSpec(R.drawable.img_ch1_c4_wanda_gaze, ChoiceAtmosphereMode.ISTANBUL_EMBER, "WANDA · AVENGERS'A KATILDI")
    "ch18_c1_help_nat_clint" -> ChoiceVisualSpec(R.drawable.img_portrait_clint_hero_1795559319365, ChoiceAtmosphereMode.COSMIC_STORM, "GÖKYÜZÜ İSKELESİ SAVUNMASI")
    "ch18_c1_help_vision_thor" -> ChoiceVisualSpec(R.drawable.img_portrait_vision_hero_1795559320364, ChoiceAtmosphereMode.COSMIC_STORM, "ÜÇLÜ TAARRUZ · SOKOVİA")
    "ch18_c2_battle_kiss_natasha" -> ChoiceVisualSpec(R.drawable.img_scene_natasha_aiden_terrace_1795640635755, ChoiceAtmosphereMode.ISTANBUL_EMBER, "♥ NATASHA · GERİ DÖN ÖPÜCÜĞÜ")
    "ch18_c2_battle_kiss_wanda" -> ChoiceVisualSpec(R.drawable.img_end_wanda_aiden_pier_1795553970888, ChoiceAtmosphereMode.ISTANBUL_EMBER, "♥ WANDA · GERİ DÖN ÖPÜCÜĞÜ")
    "ch18_c2_brotherhood_vow" -> ChoiceVisualSpec(R.drawable.img_game_rooftop_arena, ChoiceAtmosphereMode.GOLDEN_DAWN, "AVENGERS SAVAŞ YEMİNİ")
    "ch18_c3_reject_architect", "ch18_c3_tap_red_controlled" -> ChoiceVisualSpec(R.drawable.img_portrait_architect_hero_1795559252160, ChoiceAtmosphereMode.COSMIC_STORM, "ASIL MİMAR · ALTIN-MAVİ GÖZLER")
    "ch18_c4_core_guardian", "ch18_c4_flank_sweeper" -> ChoiceVisualSpec(R.drawable.img_game_rooftop_arena, ChoiceAtmosphereMode.RED_ALARM, "KİLİSE ÇEKİRDEK ÇEMBERİ")

    // ── BÖLÜM 21: KİLİSENİN SAVUNMASI & PİETRO'NUN KADERİ ──────────────────
    "ch19_c1_boost_triple_beam", "ch19_c1_sweep_evac_lane" -> ChoiceVisualSpec(R.drawable.img_anim_armada, ChoiceAtmosphereMode.COSMIC_STORM, "DÖRTLÜ KOZMİK IŞIN")
    "ch19_c2_focus_time", "ch19_c2_call_wanda" -> ChoiceVisualSpec(R.drawable.img_ch1_c1_vision_trace, ChoiceAtmosphereMode.WHITE_EYE, "QUİNJET PUSUSU · DONAN SANİYE")
    "ch19_c3_pietro_save", "ch19_c4_banter_pietro" -> ChoiceVisualSpec(R.drawable.img_portrait_pietro_hero_1795559251696, ChoiceAtmosphereMode.GOLDEN_DAWN, "★ PİETRO MAXİMOFF KURTARILDI!")
    "ch19_c3_pietro_hesitate", "ch19_c4_avenge_pietro" -> ChoiceVisualSpec(R.drawable.img_postcredits_ruined_nyc, ChoiceAtmosphereMode.RED_ALARM, "GÜMÜŞ ŞİMŞEĞİN FEDAKÂRLIĞI")

    // ── BÖLÜM 22: KIRIK AYNA — SOKOVİA'NIN KALBİ ───────────────────────────
    "ch22k_c1_calm_focus", "ch22k_c1_crimson_surge" -> ChoiceVisualSpec(R.drawable.img_portrait_voss_6b_1795557413860, ChoiceAtmosphereMode.RED_ALARM, "KATEDRAL UÇURUMU · VOSS & MİRA")
    "ch22k_c2_embrace_humanity", "ch22k_c2_burn_whisper" -> ChoiceVisualSpec(R.drawable.img_anim_blue_sky_2006, ChoiceAtmosphereMode.WHITE_EYE, "2006 KÂBUSUNU YENDİN")
    "ch22k_c3_voss_mira_evac", "ch22k_c3_voss_mira_cover", "ch22k_c4_step_into_finale" -> ChoiceVisualSpec(R.drawable.img_portrait_mira_8c_1795557413439, ChoiceAtmosphereMode.GOLDEN_DAWN, "6-B, 7-A VE 8-C KURTULDU!")

    // ── BÖLÜM 23: DEMİRİN ÇÖKÜŞÜ VE TEK YANKI (BÜYÜK FİNAL PERDE I) ───────
    "ch23f_c1_finisher_vision_wanda", "ch23f_c1_finisher_avengers_combo" -> ChoiceVisualSpec(R.drawable.img_portrait_ultron_prime_1795557413989, ChoiceAtmosphereMode.COSMIC_STORM, "ULTRON PRİME PARÇALANDI!")
    "ch23f_c2_answer_compassion", "ch23f_c2_answer_defiant" -> ChoiceVisualSpec(R.drawable.img_portrait_ultron_prime_1795557413989, ChoiceAtmosphereMode.WHITE_EYE, "ULTRON'UN SON SÖZLERİ")
    "ch23f_c3_zero_embrace", "ch23f_c3_zero_vow", "ch23f_c4_go_home" -> ChoiceVisualSpec(R.drawable.img_postcredits_twin_aiden, ChoiceAtmosphereMode.GOLDEN_DAWN, "SIFIR'IN ŞAFAK VEDASI · TEK AİDEN")

    // ── BÖLÜM 24: KÜLLERDEN DOĞAN YARIN (2. SEZON BÜYÜK FİNALİ!) ──────────
    "ch24f_c1_family_forever", "ch24f_c1_protect_future" -> ChoiceVisualSpec(R.drawable.img_anim_bosphorus, ChoiceAtmosphereMode.GOLDEN_DAWN, "VOSS & MİRA · GÜMÜŞ PUSULA")
    "ch24f_c2_forgive_self", "ch24f_c2_carry_as_shield" -> ChoiceVisualSpec(R.drawable.img_anim_blue_sky_2006, ChoiceAtmosphereMode.GOLDEN_DAWN, "2006'YA VEDA · İÇSEL BARIŞ")
    "ch24f_c3_finale_natasha", "ch24f_c4_ready_s3_nat" -> ChoiceVisualSpec(R.drawable.img_scene_natasha_aiden_terrace_1795640635755, ChoiceAtmosphereMode.GOLDEN_DAWN, "♥ NATASHA & AIDEN FİNALİ · GÜN BATIMI TERASI")
    "ch24f_c3_finale_wanda", "ch24f_c4_ready_s3_wanda" -> ChoiceVisualSpec(R.drawable.img_end_wanda_aiden_pier_1795553970888, ChoiceAtmosphereMode.GOLDEN_DAWN, "♥ WANDA & AIDEN FİNALİ · YILDIZLI GÖL İSKELESİ")
    "ch24f_c3_finale_avengers", "ch24f_c4_ready_s3_team" -> ChoiceVisualSpec(R.drawable.img_anim_bosphorus, ChoiceAtmosphereMode.GOLDEN_DAWN, "2. SEZON BÜYÜK FİNALİ · YENİ AVENGERS")

    // Smart chapter-level fallbacks for any legacy/custom saved decisions
    else -> when (chapterNumber) {
        1 -> when {
            nodeId.startsWith("ch1_s1") || orderIndex == 1 -> ChoiceVisualSpec(R.drawable.img_ch1_c2_direct_rift, ChoiceAtmosphereMode.RAIN_NEON, "İLK TEMAS")
            else -> ChoiceVisualSpec(R.drawable.img_ch1_c3_stark_faceoff, ChoiceAtmosphereMode.RAIN_NEON, "TEKLİFİN AĞIRLIĞI")
        }
        2 -> ChoiceVisualSpec(R.drawable.img_ch1_c3_stark_faceoff, ChoiceAtmosphereMode.RAIN_NEON, "CAM MASA PAZARLIĞI")
        3 -> ChoiceVisualSpec(R.drawable.img_ch1_c2_stealth_terrace, ChoiceAtmosphereMode.RED_ALARM, "DENEME SÜRESİ")
        4 -> ChoiceVisualSpec(R.drawable.img_ch1_c2_direct_rift, ChoiceAtmosphereMode.RAIN_NEON, "19 NUMARALI RIHTIM")
        5 -> ChoiceVisualSpec(R.drawable.img_ch5_voss_blizzard, ChoiceAtmosphereMode.BLIZZARD_SNOW, "KIRMIZI BUZ")
        6 -> ChoiceVisualSpec(R.drawable.img_ch6_istanbul_cistern, ChoiceAtmosphereMode.ISTANBUL_EMBER, "İSTANBUL ARŞİVİ")
        7 -> ChoiceVisualSpec(R.drawable.img_anim_bosphorus, ChoiceAtmosphereMode.GOLDEN_DAWN, "BOĞAZ'DA ŞAFAK")
        8 -> ChoiceVisualSpec(R.drawable.img_anim_armada, ChoiceAtmosphereMode.COSMIC_STORM, "KIRIK GÖKLER")
        9 -> ChoiceVisualSpec(R.drawable.img_game_rooftop_arena, ChoiceAtmosphereMode.BLIZZARD_SNOW, "KIYAMET AYNASI")
        10 -> ChoiceVisualSpec(R.drawable.img_anim_mutlak_uyanis, ChoiceAtmosphereMode.COSMIC_STORM, "YILDIZLARIN KIRILDIĞI GECE")
        11 -> ChoiceVisualSpec(R.drawable.img_ch1_c5_tower_night, ChoiceAtmosphereMode.GOLDEN_DAWN, "ESKİ DOSTLAR, YENİ GECE")
        12 -> ChoiceVisualSpec(R.drawable.img_ch1_c3_stark_faceoff, ChoiceAtmosphereMode.RED_ALARM, "ÇATLAYAN GÜVEN")
        13 -> ChoiceVisualSpec(R.drawable.img_postcredits_twin_aiden, ChoiceAtmosphereMode.COSMIC_STORM, "DONMUŞ ZAMAN VE KÂBUSLAR")
        14 -> ChoiceVisualSpec(R.drawable.img_scene_natasha_aiden_terrace_1795640635755, ChoiceAtmosphereMode.GOLDEN_DAWN, "BARTON ÇİFTLİĞİ")
        15 -> ChoiceVisualSpec(R.drawable.img_ch5_voss_blizzard, ChoiceAtmosphereMode.GOLDEN_DAWN, "KIRIK DENEKLERİN SIĞINAĞI")
        16 -> ChoiceVisualSpec(R.drawable.img_postcredits_twin_aiden, ChoiceAtmosphereMode.COSMIC_STORM, "AYNA DÜELLOSU")
        17 -> ChoiceVisualSpec(R.drawable.img_anim_blue_sky_2006, ChoiceAtmosphereMode.GOLDEN_DAWN, "VİSİON VE TEKİLLİK")
        18 -> ChoiceVisualSpec(R.drawable.img_postcredits_twin_aiden, ChoiceAtmosphereMode.RED_ALARM, "YARA İZİNİN GERÇEĞİ")
        19 -> ChoiceVisualSpec(R.drawable.img_ch5_voss_blizzard, ChoiceAtmosphereMode.GOLDEN_DAWN, "GÖLGENİN KORUYUCULARI")
        20 -> ChoiceVisualSpec(R.drawable.img_anim_armada, ChoiceAtmosphereMode.RED_ALARM, "YÜKSELEN ŞEHİR")
        21 -> ChoiceVisualSpec(R.drawable.img_anim_mutlak_uyanis, ChoiceAtmosphereMode.COSMIC_STORM, "KİLİSENİN SAVUNMASI")
        22 -> ChoiceVisualSpec(R.drawable.img_ch5_voss_blizzard, ChoiceAtmosphereMode.RED_ALARM, "KIRIK AYNA · SOKOVİA")
        23 -> ChoiceVisualSpec(R.drawable.img_postcredits_twin_aiden, ChoiceAtmosphereMode.COSMIC_STORM, "DEMİRİN ÇÖKÜŞÜ VE TEK YANKI")
        else -> ChoiceVisualSpec(R.drawable.img_anim_bosphorus, ChoiceAtmosphereMode.GOLDEN_DAWN, "KÜLLERDEN DOĞAN YARIN")
    }
}

/**
 * Formats every decision's concrete multi-axis flag shifts in crisp Turkish
 * (e.g. "Steve'in güveni +3 arttı", "Natasha'nın saygısı +3 arttı", "S.H.I.E.L.D. kuşkusu +2 arttı")
 * with small, balanced increments and "YENİ" highlights when new flags unlock.
 */
private fun buildMeaningfulImpactLines(rec: RecordedDecision): List<MeaningfulImpactLine> = buildList {
    if (rec.branchTag != null) {
        add(MeaningfulImpactLine("Hikâye Yolu: ${rec.branchTag}", Color(0xFFFEF08A), "⑂"))
    }
    if (rec.secretUnlocked != null) {
        add(MeaningfulImpactLine("Gizli İz: ${rec.secretUnlocked}", Color(0xFFFDE047), "✦"))
    }

    if (rec.flagChanges.isNotEmpty()) {
        rec.flagChanges.forEach { change ->
            val symbol = when {
                change.characterId != null && change.delta > 0 -> "♥"
                change.delta < 0 -> "▾"
                else -> "✦"
            }
            val tint = if (change.delta < 0) Color(0xFFFCA5A5) else Color(change.accentHex)
            add(
                MeaningfulImpactLine(
                    text = change.formatTurkish(),
                    tint = tint,
                    symbol = symbol,
                    isNewFlag = change.isNewlyUnlocked
                )
            )
        }
    } else {
        // Fallback for any pre-upgrade saved decision records
        rec.bondDeltas.entries
            .sortedByDescending { kotlin.math.abs(it.value) }
            .forEach { (charId, raw) ->
                if (raw == 0) return@forEach
                val capped = raw.coerceIn(-4, 4)
                val phrase = if (capped > 0) {
                    "${charId.possessiveName} güveni +$capped arttı"
                } else {
                    "${charId.possessiveName} güveni $capped azaldı"
                }
                add(
                    MeaningfulImpactLine(
                        text = phrase,
                        tint = if (capped > 0) Color(charId.accentHex) else Color(0xFFFCA5A5),
                        symbol = if (capped > 0) "♥" else "▾"
                    )
                )
            }
        if (rec.avengersTrustDelta != 0) {
            val capped = rec.avengersTrustDelta.coerceIn(-3, 3)
            val txt = if (capped > 0) "Ekip güveni +$capped arttı" else "Ekip güveni $capped azaldı"
            add(MeaningfulImpactLine(txt, if (capped > 0) Color(0xFF93C5FD) else Color(0xFFFCA5A5), "◈"))
        }
        if (rec.redEyeDelta > 0) {
            add(MeaningfulImpactLine("Kırmızı Göz dengesizliği +${rec.redEyeDelta.coerceAtMost(2)} arttı", Color(0xFFF87171), "◈"))
        }
        if (rec.fatigueDelta > 0) {
            add(MeaningfulImpactLine("Zihinsel yorgunluk +${rec.fatigueDelta.coerceAtMost(2)} arttı", Color(0xFFFBBF24), "◈"))
        }
        if (rec.mysteryDelta > 0) {
            add(MeaningfulImpactLine("Gizem perdesi +${rec.mysteryDelta.coerceAtMost(2)} arttı", Color(0xFFC4B5FD), "◈"))
        }
    }
}

/**
 * Cinematic Life is Strange 2-Style Full-Screen Chapter End Screen.
 *
 * Features:
 * - Launched when the player taps "Bölümü Bitir ✦" at the bottom of a completed chapter.
 * - Starts chapter end soundtrack (`R.raw.kozmiksuruklenismuzik` — "Kozmiksürüklenismüzik")
 *   fading from 0% to 100% volume over 1.5s when "Bölümü Bitir" is tapped.
 * - Living animated Ken Burns parallax background artwork per choice (Chapter 1 bespoke KB-sized WebP art)
 *   with smooth 1s crossfade, rain/stardust embers, and velvet cinema vignette.
 * - Unhurried Life is Strange 2 pacing:
 *   1. Opening Curtain (~2.2s)
 *   2. Sequential Decision Showcase (~8.6s per decision: 2.8s choice focus -> 5.8s consequence unfold),
 *      with Pause/Resume, Previous, Next, and "Tümünü Gör" controls.
 *   3. Final All-Choices Overview with top bar: [Çıkış] — [Bölüm İsmi] — [Sonraki Bölüm / Tekrar].
 * - Changing menus immediately triggers a smooth 2-second music fade-out (`fadeOutMs = 2000L`).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChapterSummaryModal(
    messages: List<MessageEntity>,
    bookReadState: BookReadState? = null,
    onDismiss: () -> Unit,
    onExitToBooks: () -> Unit = onDismiss,
    onRestartChapter: () -> Unit,
    onNextChapter: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val motionConfig = LocalCosmicMotionConfig.current

    val completedChapterNumber = remember(bookReadState, messages) {
        if (bookReadState != null) {
            bookReadState.activeChapter
        } else {
            val txt = messages.lastOrNull { it.role == "assistant" || it.role == "model" }?.text ?: ""
            when {
                txt.contains("BÖLÜM 7") -> 7
                txt.contains("BÖLÜM 6") -> 6
                txt.contains("BÖLÜM 5") -> 5
                txt.contains("BÖLÜM 4") -> 4
                txt.contains("BÖLÜM 3") -> 3
                txt.contains("BÖLÜM 2") -> 2
                else -> 1
            }
        }
    }

    val chapterMeta = remember(completedChapterNumber) {
        CosmicDriftBookRegistry.chapterMeta(completedChapterNumber)
    }
    val nextChapterMeta = remember(completedChapterNumber) {
        if (completedChapterNumber < CosmicDriftBookRegistry.TOTAL_PLAYABLE_CHAPTERS) {
            CosmicDriftBookRegistry.chapterMeta(completedChapterNumber + 1)
        } else {
            null
        }
    }
    val accentColor = Color(chapterMeta.accentHex)

    val slides: List<Lis2DecisionSlide> = remember(bookReadState, messages, completedChapterNumber) {
        if (bookReadState != null) {
            bookReadState.currentChapterDecisions.mapIndexed { idx, rec ->
                val vis = resolveChapterChoiceVisual(
                    chapterNumber = completedChapterNumber,
                    choiceId = rec.choiceId,
                    nodeId = rec.nodeId,
                    orderIndex = idx + 1
                )
                Lis2DecisionSlide(
                    index = idx + 1,
                    nodeId = rec.nodeId,
                    title = rec.decisionTitle,
                    selectedOption = rec.selectedOption,
                    impactText = rec.impactText,
                    isBranching = rec.isBranching,
                    isRelationship = rec.isRelationship,
                    secretUnlocked = rec.secretUnlocked,
                    branchTag = rec.branchTag,
                    artworkResId = vis.artRes,
                    atmosphere = vis.atmosphere,
                    moodTag = vis.moodTag,
                    meaningfulImpacts = buildMeaningfulImpactLines(rec)
                )
            }
        } else {
            messages.filter { it.role == "user" }.takeLast(6).mapIndexed { idx, msg ->
                val vis = resolveChapterChoiceVisual(
                    chapterNumber = completedChapterNumber,
                    choiceId = "",
                    nodeId = "",
                    orderIndex = idx + 1
                )
                Lis2DecisionSlide(
                    index = idx + 1,
                    nodeId = "custom_${idx + 1}",
                    title = "${idx + 1}. Karar",
                    selectedOption = msg.text,
                    impactText = "Bu karar hikâyenin gidişatını ve karakterlerin sana olan tutumunu şekillendirdi.",
                    isBranching = false,
                    isRelationship = true,
                    secretUnlocked = null,
                    branchTag = null,
                    artworkResId = vis.artRes,
                    atmosphere = vis.atmosphere,
                    moodTag = vis.moodTag,
                    meaningfulImpacts = emptyList()
                )
            }
        }
    }

    // Modes: PROMPT (brief 2.2s cinematic intro curtain) -> SEQUENTIAL (slow LiS2 showcase) -> FINAL_GRID (all choices)
    var viewMode by remember {
        mutableStateOf(
            if (slides.isNotEmpty()) SummaryViewMode.PROMPT else SummaryViewMode.FINAL_GRID
        )
    }
    var currentIndex by remember { mutableIntStateOf(0) }
    var lastAnimatedSlideIndex by remember { mutableIntStateOf(-1) }
    var stepPhase by remember { mutableStateOf(StepPhase.CARD_ONLY) }
    var isPaused by remember { mutableStateOf(false) }

    // Smooth auto-progress bar for each sequential slide (resumes cleanly after pause)
    val slideProgress = remember { Animatable(0f) }

    val isMusicPlaying by BookAudioController.isPlayingFlow.collectAsStateWithLifecycle()

    // Ensure "Kozmiksürüklenismüzik" ALWAYS starts automatically ON from 0 -> 100% in 1.5s
    LaunchedEffect(Unit) {
        BookAudioController.play(
            context = context,
            rawResId = R.raw.kozmiksuruklenismuzik,
            loop = true,
            volume = 1.0f,
            fadeInMs = 1500L,
            autoUnmute = true
        )
    }

    val leaveMenuWith2sFade = remember {
        { action: () -> Unit ->
            BookAudioController.stop(fadeOutMs = 2000L)
            action()
        }
    }

    BackHandler {
        leaveMenuWith2sFade(onDismiss)
    }

    // Brief atmospheric opening curtain before Choice 1 appears
    LaunchedEffect(viewMode) {
        if (viewMode == SummaryViewMode.PROMPT) {
            lastAnimatedSlideIndex = -1
            delay(2200)
            if (viewMode == SummaryViewMode.PROMPT) {
                currentIndex = 0
                stepPhase = StepPhase.CARD_ONLY
                slideProgress.snapTo(0f)
                viewMode = SummaryViewMode.SEQUENTIAL
            }
        } else if (viewMode != SummaryViewMode.SEQUENTIAL) {
            lastAnimatedSlideIndex = -1
        }
    }

    // Unhurried Life is Strange 2 Sequential Showcase Driver (~8.6 seconds per choice, pausable!)
    LaunchedEffect(viewMode, currentIndex, isPaused, slides.size) {
        if (viewMode != SummaryViewMode.SEQUENTIAL || slides.isEmpty()) return@LaunchedEffect
        if (lastAnimatedSlideIndex != currentIndex) {
            lastAnimatedSlideIndex = currentIndex
            slideProgress.snapTo(0f)
            stepPhase = StepPhase.CARD_ONLY
        }
        if (isPaused) return@LaunchedEffect

        val choiceOnlyEnd = 0.33f
        val choiceDurationMs = 2800
        val impactDurationMs = 5800

        // Stage A: Choice Moment (2.8s total)
        if (slideProgress.value < choiceOnlyEnd) {
            stepPhase = StepPhase.CARD_ONLY
            val remainingA = ((choiceOnlyEnd - slideProgress.value) / choiceOnlyEnd * choiceDurationMs)
                .toInt()
                .coerceAtLeast(120)
            slideProgress.animateTo(
                targetValue = choiceOnlyEnd,
                animationSpec = tween(durationMillis = remainingA, easing = LinearEasing)
            )
        }

        // Stage B: Unfold "Neye Etki Etti?" & meaningful impact lines (5.8s total)
        stepPhase = StepPhase.RESULT_REVEALED
        if (slideProgress.value < 1f) {
            val remainingB = ((1f - slideProgress.value) / (1f - choiceOnlyEnd) * impactDurationMs)
                .toInt()
                .coerceAtLeast(120)
            slideProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = remainingB, easing = LinearEasing)
            )
        }

        // Gentle breathing pause before smoothly transitioning to next decision or overview
        delay(260)
        if (!isPaused && viewMode == SummaryViewMode.SEQUENTIAL) {
            if (currentIndex < slides.lastIndex) {
                slideProgress.snapTo(0f)
                stepPhase = StepPhase.CARD_ONLY
                currentIndex += 1
            } else {
                viewMode = SummaryViewMode.FINAL_GRID
            }
        }
    }

    // Determine active 2.5D background artwork & atmosphere for Living Cinema Depth Stage
    val activeSlideForBackdrop: Lis2DecisionSlide? = remember(viewMode, currentIndex, slides) {
        when (viewMode) {
            SummaryViewMode.PROMPT -> slides.firstOrNull()
            SummaryViewMode.SEQUENTIAL -> slides.getOrNull(currentIndex)
            SummaryViewMode.FINAL_GRID -> slides.lastOrNull()
        }
    }
    @DrawableRes
    val activeBackdropArtRes: Int = activeSlideForBackdrop?.artworkResId
        ?: resolveChapterChoiceVisual(completedChapterNumber, "", "", 1).artRes
    val activeAtmosphere: ChoiceAtmosphereMode = activeSlideForBackdrop?.atmosphere
        ?: ChoiceAtmosphereMode.RAIN_NEON
    var userTiltYaw by remember { mutableFloatStateOf(0f) }
    var selectedFlagCharacter by remember { mutableStateOf<BookCharacterId?>(null) }
    var showStoryFlagsSheet by remember { mutableStateOf(false) }

    BackHandler(enabled = selectedFlagCharacter != null || showStoryFlagsSheet) {
        selectedFlagCharacter = null
        showStoryFlagsSheet = false
    }

    // Dynamic qualitative narrative reflection for the Overview header (no raw arcade "+11" spam)
    val relationshipReflection = remember(bookReadState, completedChapterNumber) {
        if (bookReadState == null) {
            "Verdiğin her karar bu bölümün tonunu ve geleceğini sessizce yeniden yazdı."
        } else {
            val topChar = BookStateRepository.unlockedCharacters(bookReadState)
                .maxByOrNull { bookReadState.bondScore(it) }
                ?: BookCharacterId.NATASHA
            val branchCount = bookReadState.currentChapterDecisions.count { it.isBranching }
            val secretCount = bookReadState.currentChapterDecisions.count { it.secretUnlocked != null }
            val bondSentence = when (topChar) {
                BookCharacterId.NATASHA -> "Bu bölümde en derin sessiz anlayış Natasha Romanoff ile aranda kuruldu."
                BookCharacterId.WANDA -> "Wanda Maximoff duvarlarının ardındaki gerçeği en derinden hisseden kişi oldu."
                BookCharacterId.STEVE -> "Steve Rogers'ın sana duyduğu saha güveni belirgin biçimde sağlamlaştı."
                BookCharacterId.TONY -> "Tony Stark hem zekân hem de kozmik potansiyelin karşısında derin bir merak duyuyor."
                BookCharacterId.CLINT -> "Clint Barton'ın samimiyeti ve keskin gözü Kule'de ilk kez güven duygusu verdi."
                BookCharacterId.BRUCE -> "Bruce Banner kontrol mücadeleni herkesten daha yakından anladı."
                BookCharacterId.THOR -> "Thor Odinson iradene ve kozmik gücüne içten bir saygı duydu."
                BookCharacterId.FURY -> "Nick Fury gölgelerin ardındaki hamleni takdirle izledi ve sana duyduğu saygı arttı."
                BookCharacterId.VOSS -> "Elias Voss senin koruyucu elinle karanlıktan çıkıp gerçek bir kardeşlik bağı kurdu."
                BookCharacterId.MIRA -> "Mira senin rehberliğinle korkularını aştı ve kendi gücüne güvenmeye başladı."
                BookCharacterId.PIETRO -> "Pietro Maximoff hızına ve sadakatine ilk kez gerçekten güvenmeye başladı."
                BookCharacterId.ZERO -> "Sıfır kendi karanlık geleceğini senin gözlerinde aştığını gördü."
                BookCharacterId.VISION -> "Vision, Zihin Taşı'nın gözünden senin insan kalbine derin bir saygı duydu."
            }
            val extra = buildList {
                if (branchCount > 0) add("$branchCount kritik yol ayrımına girdin")
                if (secretCount > 0) add("$secretCount gizli iz açığa çıkardın")
            }.joinToString(" ve ")
            if (extra.isNotBlank()) "$bondSentence Ayrıca $extra." else bondSentence
        }
    }

    // Continuous subtle motion loops kept as State<Float> so phase values are ONLY read inside
    // graphicsLayer{} and Canvas{} draw lambdas — eliminating 60-120fps root recomposition storms!
    val kenBurnsPhaseState: State<Float>
    val ambientDriftState: State<Float>
    val pulseGlowState: State<Float>
    if (motionConfig.enableAmbientLoops) {
        val inf = rememberInfiniteTransition(label = "lis2LivingCanvas")
        kenBurnsPhaseState = inf.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(15000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "kenBurnsZoom"
        )
        ambientDriftState = inf.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(22000, easing = LinearEasing)),
            label = "lis2Stardust"
        )
        pulseGlowState = inf.animateFloat(
            initialValue = 0.72f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Reverse),
            label = "lis2Pulse"
        )
    } else {
        kenBurnsPhaseState = remember { mutableFloatStateOf(0.35f) }
        ambientDriftState = remember { mutableFloatStateOf(0f) }
        pulseGlowState = remember { mutableFloatStateOf(0.85f) }
    }

    val noPadTextStyle = remember {
        TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(60f)
            .background(Color(0xFF070212))
            .pointerInput(viewMode, currentIndex) {
                detectHorizontalDragGestures(
                    onDragEnd = { userTiltYaw = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        userTiltYaw = (userTiltYaw + dragAmount * 0.035f).coerceIn(-10f, 10f)
                    }
                )
            }
            .testTag("chapter_end_screen_root")
    ) {
        // ── LAYER 1: TRUE 2.5D LIVING INTERACTIVE PARALLAX CHOICE ARTWORK ───
        Crossfade(
            targetState = activeBackdropArtRes,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            label = "lis2ArtworkCrossfade"
        ) { artRes ->
            Image(
                painter = painterResource(id = artRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val kenBurnsPhase = kenBurnsPhaseState.value
                        val ambientDrift = ambientDriftState.value
                        val scale = 1.03f + kenBurnsPhase * 0.04f
                        val autoYaw = sin(ambientDrift * 6.283f) * 3.0f
                        val autoPitch = cos(ambientDrift * 6.283f) * 1.8f
                        val driftX = sin(ambientDrift * 6.283f) * 10f - userTiltYaw * 1.5f
                        val driftY = cos(ambientDrift * 6.283f) * 6f
                        scaleX = scale
                        scaleY = scale
                        translationX = driftX
                        translationY = driftY
                        rotationY = (autoYaw + userTiltYaw * 0.65f).coerceIn(-12f, 12f)
                        rotationX = autoPitch
                        cameraDistance = 16f * density
                    }
            )
        }

        // ── LAYER 2: CINEMATIC LIFE IS STRANGE VIGNETTE & READABILITY SCRIM ─
        val scrimDarkness = if (viewMode == SummaryViewMode.FINAL_GRID) 0.78f else 0.54f
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to Color(0xFF090316).copy(alpha = (scrimDarkness + 0.16f).coerceAtMost(0.90f)),
                            0.24f to Color(0xFF0D0520).copy(alpha = (scrimDarkness - 0.22f).coerceAtLeast(0.24f)),
                            0.52f to Color(0xFF0A0318).copy(alpha = scrimDarkness),
                            0.82f to Color(0xFF080214).copy(alpha = (scrimDarkness + 0.20f).coerceAtMost(0.93f)),
                            1.00f to Color(0xFF060210).copy(alpha = 0.96f)
                        )
                    )
                )
        )

        // ── LAYER 3: CHOICE-MATCHED 60FPS 2.5D ATMOSPHERIC VFX OVERLAY ──────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val moodGlow = when (activeAtmosphere) {
                ChoiceAtmosphereMode.WHITE_EYE -> Color(0xFFE0F2FE)
                ChoiceAtmosphereMode.RED_ALARM -> Color(0xFFF43F5E)
                ChoiceAtmosphereMode.BLIZZARD_SNOW -> Color(0xFF7DD3FC)
                ChoiceAtmosphereMode.ISTANBUL_EMBER -> Color(0xFFFB923C)
                ChoiceAtmosphereMode.COSMIC_STORM -> Color(0xFF38BDF8)
                ChoiceAtmosphereMode.GOLDEN_DAWN -> Color(0xFFFDE047)
                ChoiceAtmosphereMode.RAIN_NEON -> accentColor
            }
            val ambientDrift = ambientDriftState.value
            val pulseGlow = pulseGlowState.value
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(moodGlow.copy(alpha = 0.20f * pulseGlow), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.22f),
                    radius = w * 0.88f
                ),
                radius = w * 0.88f,
                center = Offset(w * 0.5f, h * 0.22f)
            )
            if (motionConfig.enableAmbientLoops) {
                when (activeAtmosphere) {
                    ChoiceAtmosphereMode.WHITE_EYE -> {
                        for (r in 0..2) {
                            val p = (ambientDrift * 3f + r * 0.33f) % 1f
                            drawCircle(
                                color = Color.White.copy(alpha = (1f - p) * 0.32f),
                                radius = 34f + p * w * 0.55f,
                                center = Offset(w * 0.5f, h * 0.28f),
                                style = Stroke(width = 2f)
                            )
                        }
                    }
                    ChoiceAtmosphereMode.BLIZZARD_SNOW -> {
                        for (i in 0 until 30) {
                            val fx = ((i * 67 + 11) % 100) / 100f
                            val fy = (((i * 37 + 7) % 100) / 100f + ambientDrift * (1.2f + (i % 3) * 0.45f)) % 1f
                            val sway = sin(ambientDrift * 12.56f + i) * 22f
                            drawCircle(
                                color = Color(0xFFE0F2FE).copy(alpha = 0.45f + (i % 3) * 0.16f),
                                radius = (1.6f + (i % 3) * 0.9f).dp.toPx(),
                                center = Offset((w * fx + sway + w) % w, h * fy)
                            )
                        }
                    }
                    ChoiceAtmosphereMode.RAIN_NEON, ChoiceAtmosphereMode.COSMIC_STORM -> {
                        for (i in 0 until 26) {
                            val fx = ((i * 73 + 19) % 100) / 100f
                            val fy = (((i * 41 + 13) % 100) / 100f + ambientDrift * (2.6f + (i % 3) * 0.8f)) % 1f
                            val sx = ((fx - fy * 0.12f) + 1f) % 1f * w
                            val sy = fy * h
                            drawLine(
                                color = Color(0x48BAE6FD),
                                start = Offset(sx, sy),
                                end = Offset(sx - 5f, sy + 20f),
                                strokeWidth = 1.4f
                            )
                        }
                    }
                    ChoiceAtmosphereMode.ISTANBUL_EMBER, ChoiceAtmosphereMode.GOLDEN_DAWN, ChoiceAtmosphereMode.RED_ALARM -> {
                        for (i in 0 until 26) {
                            val fx = ((i * 71 + 13) % 100) / 100f
                            val fyBase = ((i * 43 + 17) % 100) / 100f
                            val fy = (fyBase - ambientDrift * (0.35f + (i % 3) * 0.14f) + 1f) % 1f
                            val sway = sin(ambientDrift * 6.283f + i * 0.9f) * 16f
                            val alpha = (0.22f + (i % 4) * 0.08f) * pulseGlow
                            drawCircle(
                                color = if (i % 2 == 0) moodGlow.copy(alpha = alpha) else Color(0xFFFEF08A).copy(alpha = alpha),
                                radius = (1.4f + (i % 3) * 0.8f).dp.toPx(),
                                center = Offset(w * fx + sway, h * fy)
                            )
                        }
                    }
                }
            }
        }

        // ── LAYER 4: FOREGROUND EDITORIAL UI ────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── TOP BAR: [Çıkış]  —  [Bölüm İsmi]  —  [Sonraki Bölüm / Tümünü Gör] ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val exitInteraction = remember { MutableInteractionSource() }
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .cosmicTapSpring(exitInteraction, pressedScale = 0.94f)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0x991C0C3A))
                        .border(0.95.dp, MorCamTokens.SubtleRimGradient, RoundedCornerShape(50))
                        .clickable(interactionSource = exitInteraction, indication = null) {
                            leaveMenuWith2sFade(onExitToBooks)
                        }
                        .padding(horizontal = 12.dp)
                        .testTag("chapter_end_exit_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Çıkış",
                        tint = MorCamTokens.SilverLavenderBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Çıkış",
                        style = noPadTextStyle,
                        fontFamily = InterFontFamily,
                        color = MorCamTokens.SilverLavenderBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Center Chapter Name (clean, centered, zero font padding clipping)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = accentColor.copy(alpha = 0.9f),
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "BÖLÜM ${chapterMeta.romanNumeral} SONU",
                            style = noPadTextStyle,
                            fontFamily = InterFontFamily,
                            color = accentColor,
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = chapterMeta.title,
                        style = noPadTextStyle,
                        fontFamily = DisplaySerifFontFamily,
                        color = Color(0xFFFFFFFF),
                        fontSize = 16.5.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }

                // Music On/Off Toggle Orb (lives right on the Chapter End Screen)
                val musicToggleInteraction = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .cosmicTapSpring(musicToggleInteraction, pressedScale = 0.92f)
                        .then(
                            if (isMusicPlaying) {
                                Modifier.morCamCosmicGlow(
                                    cornerRadius = 20.dp,
                                    glowColor = accentColor,
                                    alpha = 0.38f,
                                    blurDp = 9.dp
                                )
                            } else {
                                Modifier
                            }
                        )
                        .clip(CircleShape)
                        .background(if (isMusicPlaying) Color(0xB82A1256) else Color(0x8A170A32))
                        .border(
                            width = if (isMusicPlaying) 1.1.dp else 0.9.dp,
                            brush = if (isMusicPlaying) MorCamTokens.RimGradient else MorCamTokens.SubtleRimGradient,
                            shape = CircleShape
                        )
                        .clickable(interactionSource = musicToggleInteraction, indication = null) {
                            BookAudioController.toggleMusicInChapterEnd(
                                context = context,
                                rawResId = R.raw.kozmiksuruklenismuzik,
                                fullVolume = 1.0f
                            )
                        }
                        .testTag("chapter_end_music_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMusicPlaying) Icons.Default.MusicNote else Icons.Default.MusicOff,
                        contentDescription = if (isMusicPlaying) "Müziği Kapat" else "Müziği Aç",
                        tint = if (isMusicPlaying) Color(0xFFF3E8FF) else MorCamTokens.LavenderMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Right Top Bar Action
                if (viewMode != SummaryViewMode.FINAL_GRID) {
                    val skipInteraction = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .cosmicTapSpring(skipInteraction, pressedScale = 0.94f)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0x881F0E42))
                            .border(0.95.dp, Color(0x88D8B4FE), RoundedCornerShape(50))
                            .clickable(interactionSource = skipInteraction, indication = null) {
                                viewMode = SummaryViewMode.FINAL_GRID
                            }
                            .padding(horizontal = 11.dp)
                            .testTag("chapter_end_skip_sequence_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tümünü Gör",
                            style = noPadTextStyle,
                            fontFamily = InterFontFamily,
                            color = Color(0xFFEDE6FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else if (onNextChapter != null && nextChapterMeta != null) {
                    val nextInteraction = remember { MutableInteractionSource() }
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .cosmicTapSpring(nextInteraction, pressedScale = 0.94f)
                            .morCamCosmicGlow(cornerRadius = 20.dp, glowColor = accentColor, alpha = 0.44f, blurDp = 10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MorCamTokens.PrimaryButtonFill)
                            .border(1.1.dp, MorCamTokens.RimGradient, RoundedCornerShape(50))
                            .clickable(interactionSource = nextInteraction, indication = null) {
                                leaveMenuWith2sFade(onNextChapter)
                            }
                            .padding(horizontal = 11.dp)
                            .testTag("chapter_end_top_next_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Bölüm ${nextChapterMeta.number}",
                            style = noPadTextStyle,
                            fontFamily = InterFontFamily,
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Sonraki Bölüm",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                } else {
                    val replayInteraction = remember { MutableInteractionSource() }
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .cosmicTapSpring(replayInteraction, pressedScale = 0.94f)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0x9926124E))
                            .border(1.dp, MorCamTokens.RimGradient, RoundedCornerShape(50))
                            .clickable(interactionSource = replayInteraction, indication = null) {
                                leaveMenuWith2sFade(onRestartChapter)
                            }
                            .padding(horizontal = 10.dp)
                            .testTag("chapter_end_top_replay_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Tekrar",
                            tint = MorCamTokens.SilverLavenderBright,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Tekrar",
                            style = noPadTextStyle,
                            fontFamily = InterFontFamily,
                            color = MorCamTokens.SilverLavenderBright,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Subtle Soundtrack Status Pill displaying "Kozmiksürüklenismüzik"
            val trackPillInteraction = remember { MutableInteractionSource() }
            Row(
                modifier = Modifier
                    .cosmicTapSpring(trackPillInteraction, pressedScale = 0.96f)
                    .clip(RoundedCornerShape(50))
                    .background(if (isMusicPlaying) Color(0x70210E46) else Color(0x5514082C))
                    .border(
                        width = 0.75.dp,
                        color = if (isMusicPlaying) accentColor.copy(alpha = 0.45f) else Color(0x40A78BFA),
                        shape = RoundedCornerShape(50)
                    )
                    .clickable(interactionSource = trackPillInteraction, indication = null) {
                        BookAudioController.toggleMusicInChapterEnd(
                            context = context,
                            rawResId = R.raw.kozmiksuruklenismuzik,
                            fullVolume = 1.0f
                        )
                    }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .testTag("chapter_end_track_pill"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = if (isMusicPlaying) Icons.Default.MusicNote else Icons.Default.MusicOff,
                    contentDescription = null,
                    tint = if (isMusicPlaying) accentColor else MorCamTokens.LavenderMuted,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = if (isMusicPlaying) {
                        "${BookAudioController.TRACK_DISPLAY_NAME}  ·  Açık"
                    } else {
                        "${BookAudioController.TRACK_DISPLAY_NAME}  ·  Kapalı"
                    },
                    style = noPadTextStyle,
                    fontFamily = InterFontFamily,
                    color = if (isMusicPlaying) Color(0xFFE9DDFF) else MorCamTokens.LavenderMuted,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── STAGE BODY ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            ) {
                AnimatedContent(
                    targetState = viewMode,
                    transitionSpec = {
                        (fadeIn(tween(650)) + scaleIn(initialScale = 0.97f, animationSpec = tween(650))) togetherWith
                            fadeOut(tween(420))
                    },
                    label = "lis2PhaseTransition"
                ) { mode ->
                    when (mode) {
                        // ── INTRO CURTAIN (2.2s cinematic breathing beat) ───
                        SummaryViewMode.PROMPT -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        currentIndex = 0
                                        stepPhase = StepPhase.CARD_ONLY
                                        viewMode = SummaryViewMode.SEQUENTIAL
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "BÖLÜM ${chapterMeta.romanNumeral} TAMAMLANDI",
                                    style = noPadTextStyle,
                                    fontFamily = InterFontFamily,
                                    color = Color(0xFFFEF08A),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.4.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = chapterMeta.title,
                                    fontFamily = DisplaySerifFontFamily,
                                    color = Color.White,
                                    fontSize = 34.sp,
                                    lineHeight = 40.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Box(
                                    modifier = Modifier
                                        .width(86.dp)
                                        .height(1.2.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color.Transparent, accentColor, Color.Transparent)
                                            )
                                        )
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Verdiğin kararlar yolunu çizdi...",
                                    fontFamily = LiterarySerifFontFamily,
                                    fontStyle = FontStyle.Italic,
                                    color = Color(0xFFE6DCFF),
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // ── PHASE 1: LIFE IS STRANGE 2 SEQUENTIAL CHOICE SHOWCASE ──
                        SummaryViewMode.SEQUENTIAL -> {
                            val activeSlide = slides.getOrNull(currentIndex)
                            if (activeSlide != null) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Top Segmented Story Progress Bars (Life is Strange / Cinema style)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        slides.indices.forEach { idx ->
                                            val fillFraction = when {
                                                idx < currentIndex -> 1f
                                                idx == currentIndex -> slideProgress.value.coerceIn(0f, 1f)
                                                else -> 0f
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(3.2.dp)
                                                    .clip(RoundedCornerShape(50))
                                                    .background(Color(0x45FFFFFF))
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth(fillFraction)
                                                        .fillMaxHeight()
                                                        .clip(RoundedCornerShape(50))
                                                        .background(
                                                            Brush.horizontalGradient(
                                                                listOf(accentColor, Color(0xFFFAF6FF))
                                                            )
                                                        )
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Keep top ~26% open so the living choice artwork breathes cinematically!
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth(),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        AnimatedContent(
                                            targetState = activeSlide,
                                            transitionSpec = {
                                                (fadeIn(tween(620)) + slideInVertically(
                                                    initialOffsetY = { it / 8 },
                                                    animationSpec = tween(620)
                                                )) togetherWith fadeOut(tween(340))
                                            },
                                            label = "lis2SlideCard"
                                        ) { card ->
                                            val badgeColor = when {
                                                card.isBranching -> Color(0xFFFEF08A)
                                                card.secretUnlocked != null -> Color(0xFFFDE047)
                                                card.isRelationship -> Color(0xFFF9A8D4)
                                                else -> accentColor
                                            }
                                            val badgeLabel = when {
                                                card.isBranching -> "KRİTİK YOL AYRIMI"
                                                card.secretUnlocked != null -> "GİZLİ İZ AÇILDI"
                                                card.isRelationship -> "BAĞ & İLİŞKİ ANI"
                                                else -> "KADER SEÇİMİ"
                                            }

                                            val slideScrollState = rememberScrollState()
                                            val slideTopFade by animateFloatAsState(
                                                targetValue = if (slideScrollState.value > 6) 1f else 0f,
                                                animationSpec = tween(180),
                                                label = "summary_slide_top_fade"
                                            )
                                            val slideBottomFade by animateFloatAsState(
                                                targetValue = if (slideScrollState.maxValue > 0 && slideScrollState.value < slideScrollState.maxValue - 6) 1f else 0f,
                                                animationSpec = tween(180),
                                                label = "summary_slide_bottom_fade"
                                            )
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .verticalFadingEdge(
                                                        topFadeHeight = 20.dp,
                                                        bottomFadeHeight = 24.dp,
                                                        topFadeAlpha = slideTopFade,
                                                        bottomFadeAlpha = slideBottomFade
                                                    )
                                                    .verticalScroll(slideScrollState)
                                                    .padding(top = 4.dp, bottom = 6.dp),
                                                verticalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                // Editorial Decision Header floating softly over the artwork
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 4.dp),
                                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Text(
                                                            text = "0${card.index} / 0${slides.size}",
                                                            style = noPadTextStyle,
                                                            fontFamily = DisplaySerifFontFamily,
                                                            color = Color(0xFFE9D5FF),
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            letterSpacing = 1.4.sp
                                                        )
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .clip(CircleShape)
                                                                .background(badgeColor)
                                                        )
                                                        Row(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(50))
                                                                .background(Color(0x8815082E))
                                                                .border(0.85.dp, badgeColor.copy(alpha = 0.65f), RoundedCornerShape(50))
                                                                .padding(horizontal = 9.dp, vertical = 4.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = when {
                                                                    card.isBranching -> Icons.Default.Route
                                                                    card.isRelationship -> Icons.Default.Favorite
                                                                    else -> Icons.Default.AutoAwesome
                                                                },
                                                                contentDescription = null,
                                                                tint = badgeColor,
                                                                modifier = Modifier.size(12.dp)
                                                            )
                                                            Text(
                                                                text = badgeLabel,
                                                                style = noPadTextStyle,
                                                                fontFamily = InterFontFamily,
                                                                color = badgeColor,
                                                                fontSize = 9.5.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                letterSpacing = 0.9.sp
                                                            )
                                                        }
                                                    }

                                                    Text(
                                                        text = card.title,
                                                        fontFamily = DisplaySerifFontFamily,
                                                        color = Color(0xFFFFFFFF),
                                                        fontSize = 26.sp,
                                                        lineHeight = 31.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }

                                                // Translucent Frosted Mor Cam Narrative Card
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .morCamCosmicGlow(
                                                            cornerRadius = 26.dp,
                                                            glowColor = badgeColor,
                                                            alpha = 0.31f,
                                                            blurDp = 18.dp
                                                        )
                                                        .clip(RoundedCornerShape(26.dp))
                                                        .background(
                                                            Brush.verticalGradient(
                                                                listOf(
                                                                    Color(0xCC1E0C42),
                                                                    Color(0xDD100526),
                                                                    Color(0xD9160832)
                                                                )
                                                            )
                                                        )
                                                        .border(
                                                            width = 1.2.dp,
                                                            brush = Brush.linearGradient(
                                                                listOf(
                                                                    Color(0xDDFAF5FF),
                                                                    badgeColor.copy(alpha = 0.8f),
                                                                    Color(0x888B5CF6)
                                                                )
                                                            ),
                                                            shape = RoundedCornerShape(26.dp)
                                                        )
                                                        .padding(horizontal = 20.dp, vertical = 20.dp)
                                                        .testTag("lis2_active_decision_card")
                                                ) {
                                                    Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                                                        Text(
                                                            text = "SENİN SEÇİMİN",
                                                            style = noPadTextStyle,
                                                            fontFamily = InterFontFamily,
                                                            color = MorCamTokens.LavenderSoft,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            letterSpacing = 1.4.sp
                                                        )

                                                        Text(
                                                            text = "“${card.selectedOption}”",
                                                            fontFamily = LiterarySerifFontFamily,
                                                            color = Color(0xFFFFFFFF),
                                                            fontSize = 16.5.sp,
                                                            lineHeight = 24.5.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )

                                                        // Stage B: Smoothly unfolds the narrative meaning & consequences
                                                        AnimatedVisibility(
                                                            visible = stepPhase == StepPhase.RESULT_REVEALED,
                                                            enter = expandVertically(
                                                                animationSpec = spring(
                                                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                                                    stiffness = Spring.StiffnessLow
                                                                )
                                                            ) + fadeIn(tween(550))
                                                        ) {
                                                            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                                                                MorCamDivider()

                                                                Row(
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.AutoAwesome,
                                                                        contentDescription = null,
                                                                        tint = Color(0xFFFEF08A),
                                                                        modifier = Modifier.size(14.dp)
                                                                    )
                                                                    Text(
                                                                        text = "NEYE ETKİ ETTİ?",
                                                                        style = noPadTextStyle,
                                                                        fontFamily = InterFontFamily,
                                                                        color = Color(0xFFFEF08A),
                                                                        fontSize = 10.5.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        letterSpacing = 1.3.sp
                                                                    )
                                                                }

                                                                Text(
                                                                    text = card.impactText,
                                                                    fontFamily = LiterarySerifFontFamily,
                                                                    fontStyle = FontStyle.Italic,
                                                                    color = Color(0xFFF0E8FF),
                                                                    fontSize = 15.sp,
                                                                    lineHeight = 23.sp
                                                                )

                                                                // Concrete balanced flag shifts ("Steve'in güveni +3 arttı" etc.)
                                                                if (card.meaningfulImpacts.isNotEmpty()) {
                                                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                                        card.meaningfulImpacts.forEach { line ->
                                                                            Row(
                                                                                modifier = Modifier
                                                                                    .fillMaxWidth()
                                                                                    .clip(RoundedCornerShape(12.dp))
                                                                                    .background(line.tint.copy(alpha = 0.14f))
                                                                                    .border(
                                                                                        0.8.dp,
                                                                                        line.tint.copy(alpha = 0.45f),
                                                                                        RoundedCornerShape(12.dp)
                                                                                    )
                                                                                    .padding(horizontal = 11.dp, vertical = 7.dp),
                                                                                verticalAlignment = Alignment.CenterVertically,
                                                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                                            ) {
                                                                                Text(
                                                                                    text = line.symbol,
                                                                                    style = noPadTextStyle,
                                                                                    color = line.tint,
                                                                                    fontSize = 12.sp,
                                                                                    fontWeight = FontWeight.Bold
                                                                                )
                                                                                Text(
                                                                                    text = line.text,
                                                                                    fontFamily = InterFontFamily,
                                                                                    color = Color(0xFFFAF6FF),
                                                                                    fontSize = 12.2.sp,
                                                                                    lineHeight = 16.5.sp,
                                                                                    fontWeight = FontWeight.SemiBold,
                                                                                    modifier = Modifier.weight(1f)
                                                                                )
                                                                                if (line.isNewFlag) {
                                                                                    Box(
                                                                                        modifier = Modifier
                                                                                            .clip(RoundedCornerShape(50))
                                                                                            .background(Color(0x38FEF08A))
                                                                                            .border(0.7.dp, Color(0x99FEF08A), RoundedCornerShape(50))
                                                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                                                    ) {
                                                                                        Text(
                                                                                            text = "YENİ",
                                                                                            style = noPadTextStyle,
                                                                                            fontFamily = InterFontFamily,
                                                                                            color = Color(0xFFFEF08A),
                                                                                            fontSize = 8.5.sp,
                                                                                            fontWeight = FontWeight.Bold,
                                                                                            letterSpacing = 0.8.sp
                                                                                        )
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Calm Playback Controls: [←]  [Duraklat / Devam]  [Sonraki →]
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Previous Choice Button
                                        val prevInteraction = remember { MutableInteractionSource() }
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .cosmicTapSpring(prevInteraction, pressedScale = 0.9f)
                                                .clip(CircleShape)
                                                .background(
                                                    if (currentIndex > 0) Color(0x99231048) else Color(0x40160A2E)
                                                )
                                                .border(
                                                    0.95.dp,
                                                    if (currentIndex > 0) Color(0x88D8B4FE) else Color(0x33D8B4FE),
                                                    CircleShape
                                                )
                                                .clickable(
                                                    enabled = currentIndex > 0,
                                                    interactionSource = prevInteraction,
                                                    indication = null
                                                ) {
                                                    isPaused = false
                                                    stepPhase = StepPhase.CARD_ONLY
                                                    currentIndex -= 1
                                                }
                                                .testTag("lis2_prev_decision_btn"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Önceki Karar",
                                                tint = if (currentIndex > 0) Color.White else Color(0x55FFFFFF),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // Pause / Play Toggle Orb
                                        val pauseInteraction = remember { MutableInteractionSource() }
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .cosmicTapSpring(pauseInteraction, pressedScale = 0.9f)
                                                .clip(CircleShape)
                                                .background(Color(0x99231048))
                                                .border(0.95.dp, Color(0x88D8B4FE), CircleShape)
                                                .clickable(interactionSource = pauseInteraction, indication = null) {
                                                    isPaused = !isPaused
                                                }
                                                .testTag("lis2_pause_toggle_btn"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                                contentDescription = if (isPaused) "Devam Et" else "Duraklat",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        // Next / Overview Button
                                        MorCamCapsuleButton(
                                            text = if (currentIndex < slides.lastIndex) {
                                                "Sonraki (${currentIndex + 2}/${slides.size}) →"
                                            } else {
                                                "Tüm Seçimleri Gör ✦"
                                            },
                                            onClick = {
                                                isPaused = false
                                                if (currentIndex < slides.lastIndex) {
                                                    stepPhase = StepPhase.CARD_ONLY
                                                    currentIndex += 1
                                                } else {
                                                    viewMode = SummaryViewMode.FINAL_GRID
                                                }
                                            },
                                            variant = MorCamButtonVariant.Primary,
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("summary_sequential_next_btn")
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }
                        }

                        // ── PHASE 2: ALL CHOICES & MEANINGFUL CONSEQUENCES BOARD ──
                        SummaryViewMode.FINAL_GRID -> {
                            val gridScrollState = rememberScrollState()
                            val gridTopFade by animateFloatAsState(
                                targetValue = if (gridScrollState.value > 6) 1f else 0f,
                                animationSpec = tween(180),
                                label = "summary_grid_top_fade"
                            )
                            val gridBottomFade by animateFloatAsState(
                                targetValue = if (gridScrollState.maxValue > 0 && gridScrollState.value < gridScrollState.maxValue - 6) 1f else 0f,
                                animationSpec = tween(180),
                                label = "summary_grid_bottom_fade"
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalFadingEdge(
                                        topFadeHeight = 24.dp,
                                        bottomFadeHeight = 32.dp,
                                        topFadeAlpha = gridTopFade,
                                        bottomFadeAlpha = gridBottomFade
                                    )
                                    .verticalScroll(gridScrollState)
                                    .padding(top = 6.dp, bottom = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Narrative Chapter Reflection Banner with Aidem's Canonical Portrait
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .morCamCosmicGlow(cornerRadius = 22.dp, glowColor = accentColor, alpha = 0.31f, blurDp = 14.dp)
                                        .clip(RoundedCornerShape(22.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0x99381874), Color(0x8A170838))
                                            )
                                        )
                                        .border(1.1.dp, accentColor.copy(alpha = 0.72f), RoundedCornerShape(22.dp))
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 62.dp, height = 82.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .border(1.1.dp, accentColor.copy(alpha = 0.75f), RoundedCornerShape(14.dp))
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.img_aiden_portrait),
                                                contentDescription = "Aiden Blackwood",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "✦ BÖLÜM ${chapterMeta.romanNumeral} · KARARLARIN VE BAYRAKLAR",
                                                style = noPadTextStyle,
                                                fontFamily = InterFontFamily,
                                                color = Color(0xFFFEF08A),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            )
                                            Text(
                                                text = relationshipReflection,
                                                fontFamily = LiterarySerifFontFamily,
                                                fontStyle = FontStyle.Italic,
                                                color = Color(0xFFFAF6FF),
                                                fontSize = 13.8.sp,
                                                lineHeight = 20.5.sp
                                            )
                                        }
                                    }
                                }

                                // All Choices Together (with Choice-Specific Artwork Strips for Chapter 1!)
                                slides.forEachIndexed { idx, card ->
                                    val cardTint = when {
                                        card.isBranching -> Color(0xFFFEF08A)
                                        card.secretUnlocked != null -> Color(0xFFFDE047)
                                        card.isRelationship -> Color(0xFFF9A8D4)
                                        else -> accentColor
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .cosmicStaggeredEnter(index = idx, key = "overview_${card.index}")
                                            .clip(RoundedCornerShape(22.dp))
                                            .background(Color(0xCC160830))
                                            .border(1.dp, cardTint.copy(alpha = 0.52f), RoundedCornerShape(22.dp))
                                            .clickable {
                                                currentIndex = idx
                                                isPaused = true
                                                stepPhase = StepPhase.RESULT_REVEALED
                                                viewMode = SummaryViewMode.SEQUENTIAL
                                            }
                                    ) {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            // 2.5D Living Event- & Choice-Specific Artwork Banner (ALL 10 Season 1 Chapters!)
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(108.dp)
                                                    .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                                            ) {
                                                Image(
                                                    painter = painterResource(id = card.artworkResId),
                                                    contentDescription = card.title,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .graphicsLayer {
                                                            val cardScale = 1.05f + kenBurnsPhaseState.value * 0.07f
                                                            val cardTilt = sin(ambientDriftState.value * 6.283f + idx * 1.1f) * 3.5f
                                                            scaleX = cardScale
                                                            scaleY = cardScale
                                                            translationX = cardTilt * 2.5f
                                                            rotationY = cardTilt
                                                            cameraDistance = 14f * density
                                                        }
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(
                                                            Brush.verticalGradient(
                                                                listOf(
                                                                    Color(0x280D0420),
                                                                    Color(0x8A13062A),
                                                                    Color(0xEE160830)
                                                                )
                                                            )
                                                        )
                                                )
                                                // Top-right 2.5D Scene Mood Pill
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(8.dp)
                                                        .clip(RoundedCornerShape(50))
                                                        .background(Color(0xB80A041C))
                                                        .border(0.7.dp, cardTint.copy(alpha = 0.65f), RoundedCornerShape(50))
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                ) {
                                                    Text(
                                                        text = "✦ 2.5D · ${card.moodTag}",
                                                        style = noPadTextStyle,
                                                        fontFamily = InterFontFamily,
                                                        color = Color(0xFFFAF6FF),
                                                        fontSize = 8.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = 0.6.sp
                                                    )
                                                }
                                                Row(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomStart)
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        modifier = Modifier.weight(1f),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(24.dp)
                                                                .clip(CircleShape)
                                                                .background(cardTint.copy(alpha = 0.28f))
                                                                .border(1.dp, cardTint.copy(alpha = 0.85f), CircleShape),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = "${card.index}",
                                                                style = noPadTextStyle,
                                                                fontFamily = InterFontFamily,
                                                                color = Color.White,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                        Text(
                                                            text = card.title,
                                                            fontFamily = DisplaySerifFontFamily,
                                                            color = Color.White,
                                                            fontSize = 16.5.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                    if (card.isBranching) {
                                                        Text(
                                                            text = "⑂ Yol Ayrımı",
                                                            style = noPadTextStyle,
                                                            fontFamily = InterFontFamily,
                                                            color = Color(0xFFFEF08A),
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }

                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 15.dp, vertical = 13.dp),
                                                verticalArrangement = Arrangement.spacedBy(7.dp)
                                            ) {

                                                Text(
                                                    text = "“${card.selectedOption}”",
                                                    fontFamily = LiterarySerifFontFamily,
                                                    color = Color(0xFFFFFFFF),
                                                    fontSize = 14.sp,
                                                    lineHeight = 20.5.sp,
                                                    fontWeight = FontWeight.Medium
                                                )

                                                Text(
                                                    text = card.impactText,
                                                    fontFamily = InterFontFamily,
                                                    color = Color(0xFFD9C9FA),
                                                    fontSize = 12.2.sp,
                                                    lineHeight = 18.sp
                                                )

                                                if (card.meaningfulImpacts.isNotEmpty()) {
                                                    FlowRow(
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        verticalArrangement = Arrangement.spacedBy(5.dp),
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    ) {
                                                        card.meaningfulImpacts.forEach { impact ->
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(50))
                                                                    .background(impact.tint.copy(alpha = 0.14f))
                                                                    .border(0.75.dp, impact.tint.copy(alpha = 0.45f), RoundedCornerShape(50))
                                                                    .padding(horizontal = 9.dp, vertical = 4.dp)
                                                            ) {
                                                                Text(
                                                                    text = "${impact.symbol} ${impact.text}",
                                                                    style = noPadTextStyle,
                                                                    fontFamily = InterFontFamily,
                                                                    color = Color(0xFFFAF6FF),
                                                                    fontSize = 10.2.sp,
                                                                    fontWeight = FontWeight.Medium
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Compact Character Names & Story Flags Picker (tap any name -> sleek partial bottom sheet!)
                                if (bookReadState != null) {
                                    val unlockedChars = remember(
                                        bookReadState.activeChapter,
                                        bookReadState.highestCompletedChapter,
                                        bookReadState.currentNodeId,
                                        bookReadState.numericFlags
                                    ) {
                                        BookStateRepository.unlockedCharacters(bookReadState)
                                    }
                                    val unlockedStoryFlags = remember(bookReadState.numericFlags) {
                                        bookReadState.numericFlags.entries
                                            .filter { it.key.startsWith("story:") && it.value != 0 }
                                            .sortedByDescending { kotlin.math.abs(it.value) }
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color(0xB8160830))
                                            .border(0.95.dp, MorCamTokens.SubtleRimGradient, RoundedCornerShape(20.dp))
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "KARAKTER BAYRAKLARI",
                                                style = noPadTextStyle,
                                                fontFamily = InterFontFamily,
                                                color = Color(0xFFFEF08A),
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.1.sp
                                            )
                                            Text(
                                                text = "${unlockedChars.size} Karakter • Dokun",
                                                style = noPadTextStyle,
                                                fontFamily = InterFontFamily,
                                                color = MorCamTokens.LavenderSoft,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                                            verticalArrangement = Arrangement.spacedBy(7.dp)
                                        ) {
                                            unlockedChars.forEach { charId ->
                                                val cColor = Color(charId.accentHex)
                                                val flagCount = remember(bookReadState.numericFlags, charId) {
                                                    BookStateRepository.characterFlagsFor(bookReadState, charId).size
                                                }
                                                val chipInteraction = remember { MutableInteractionSource() }
                                                Row(
                                                    modifier = Modifier
                                                        .cosmicTapSpring(chipInteraction, pressedScale = 0.94f)
                                                        .clip(RoundedCornerShape(50))
                                                        .background(cColor.copy(alpha = if (flagCount > 0) 0.16f else 0.08f))
                                                        .border(
                                                            width = 0.85.dp,
                                                            color = cColor.copy(alpha = if (flagCount > 0) 0.65f else 0.35f),
                                                            shape = RoundedCornerShape(50)
                                                        )
                                                        .clickable(interactionSource = chipInteraction, indication = null) {
                                                            showStoryFlagsSheet = false
                                                            selectedFlagCharacter = charId
                                                        }
                                                        .padding(start = 5.dp, end = 11.dp, top = 4.dp, bottom = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    CinematicCharacterAvatar(
                                                        key = CharacterPortraitKey.fromBookCharacter(charId),
                                                        size = 22.dp,
                                                        borderWidth = 1.dp,
                                                        borderColor = cColor
                                                    )
                                                    Text(
                                                        text = charId.shortName,
                                                        style = noPadTextStyle,
                                                        fontFamily = InterFontFamily,
                                                        color = Color(0xFFFAF6FF),
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    if (flagCount > 0) {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(CircleShape)
                                                                .background(cColor.copy(alpha = 0.28f))
                                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                                        ) {
                                                            Text(
                                                                text = "$flagCount",
                                                                style = noPadTextStyle,
                                                                fontFamily = InterFontFamily,
                                                                color = cColor,
                                                                fontSize = 9.5.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            if (unlockedStoryFlags.isNotEmpty()) {
                                                val storyChipInteraction = remember { MutableInteractionSource() }
                                                Row(
                                                    modifier = Modifier
                                                        .cosmicTapSpring(storyChipInteraction, pressedScale = 0.94f)
                                                        .clip(RoundedCornerShape(50))
                                                        .background(Color(0x28FEF08A))
                                                        .border(0.85.dp, Color(0x77FEF08A), RoundedCornerShape(50))
                                                        .clickable(interactionSource = storyChipInteraction, indication = null) {
                                                            selectedFlagCharacter = null
                                                            showStoryFlagsSheet = true
                                                        }
                                                        .padding(horizontal = 11.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                ) {
                                                    Text(
                                                        text = "✦ Evren İzleri",
                                                        style = noPadTextStyle,
                                                        fontFamily = InterFontFamily,
                                                        color = Color(0xFFFEF9C3),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "${unlockedStoryFlags.size}",
                                                        style = noPadTextStyle,
                                                        fontFamily = InterFontFamily,
                                                        color = Color(0xFFFEF08A),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = "✦ İsmine dokunduğun karakterin bayrakları altta açılan kompakt pencerede görünür.",
                                            fontFamily = InterFontFamily,
                                            fontStyle = FontStyle.Italic,
                                            color = MorCamTokens.LavenderSoft.copy(alpha = 0.85f),
                                            fontSize = 10.5.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Primary Bottom CTA: Next Chapter
                                if (onNextChapter != null && nextChapterMeta != null) {
                                    MorCamCapsuleButton(
                                        text = "Bölüm ${nextChapterMeta.number}: ${nextChapterMeta.title} →",
                                        onClick = { leaveMenuWith2sFade(onNextChapter) },
                                        variant = MorCamButtonVariant.Primary,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("summary_next_chapter_btn")
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    MorCamCapsuleButton(
                                        text = "Bölümü Tekrar Oyna",
                                        onClick = { leaveMenuWith2sFade(onRestartChapter) },
                                        variant = MorCamButtonVariant.Secondary,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("summary_replay_btn")
                                    )
                                    MorCamCapsuleButton(
                                        text = "Metne Dön",
                                        onClick = { leaveMenuWith2sFade(onDismiss) },
                                        variant = MorCamButtonVariant.Secondary,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("summary_return_btn")
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                            }
                        }
                    }
                }
            }
        }

        // ── PARTIAL-HEIGHT SCROLLABLE CHARACTER / STORY FLAGS BOTTOM SHEET ──
        val isSheetOpen = bookReadState != null && (selectedFlagCharacter != null || showStoryFlagsSheet)
        androidx.compose.animation.AnimatedVisibility(
            visible = isSheetOpen,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(180)),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(25f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xA6050210))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        selectedFlagCharacter = null
                        showStoryFlagsSheet = false
                    }
            )
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = isSheetOpen,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(tween(200)),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(tween(180)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(26f)
        ) {
                if (bookReadState != null) {
                    val activeChar = selectedFlagCharacter
                    val sheetAccent = if (activeChar != null) Color(activeChar.accentHex) else Color(0xFFFEF08A)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 580.dp)
                            .fillMaxHeight(0.52f)
                            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFA1E0D42), Color(0xFC100525), Color(0xFF0A0318))
                                )
                            )
                            .border(
                                width = 1.25.dp,
                                brush = Brush.linearGradient(
                                    listOf(sheetAccent.copy(alpha = 0.85f), Color(0x66C4B5FD), Color.Transparent)
                                ),
                                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { /* consume taps inside sheet */ }
                            .navigationBarsPadding()
                            .padding(horizontal = 18.dp, vertical = 12.dp)
                    ) {
                        // Top Drag Handle Pill
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .size(width = 40.dp, height = 4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0x66D8B4FE))
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Sheet Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (activeChar != null) {
                                CinematicCharacterAvatar(
                                    key = CharacterPortraitKey.fromBookCharacter(activeChar),
                                    size = 46.dp,
                                    borderWidth = 1.5.dp,
                                    borderColor = sheetAccent
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = activeChar.displayName,
                                        fontFamily = DisplaySerifFontFamily,
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = activeChar.roleTitle,
                                        fontFamily = InterFontFamily,
                                        color = sheetAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "✦ Evren & Hikâye Bayrakları",
                                        fontFamily = DisplaySerifFontFamily,
                                        color = Color(0xFFFEF08A),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Seçimlerinin dünya ve psikolojik dengeler üzerindeki izi",
                                        fontFamily = InterFontFamily,
                                        color = MorCamTokens.LavenderMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x552C1654))
                                    .border(0.9.dp, Color(0x88D8B4FE), CircleShape)
                                    .clickable {
                                        selectedFlagCharacter = null
                                        showStoryFlagsSheet = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Kapat",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        MorCamDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        // Scrollable List of ONLY this character's (or story's) flags
                        val flagSheetScrollState = rememberScrollState()
                        val flagTopFade by animateFloatAsState(
                            targetValue = if (flagSheetScrollState.value > 6) 1f else 0f,
                            animationSpec = tween(180),
                            label = "flag_sheet_top_fade"
                        )
                        val flagBottomFade by animateFloatAsState(
                            targetValue = if (flagSheetScrollState.maxValue > 0 && flagSheetScrollState.value < flagSheetScrollState.maxValue - 6) 1f else 0f,
                            animationSpec = tween(180),
                            label = "flag_sheet_bottom_fade"
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalFadingEdge(
                                    topFadeHeight = 18.dp,
                                    bottomFadeHeight = 24.dp,
                                    topFadeAlpha = flagTopFade,
                                    bottomFadeAlpha = flagBottomFade
                                )
                                .verticalScroll(flagSheetScrollState)
                                .padding(top = 2.dp, bottom = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            if (activeChar != null) {
                                val flagsForChar = remember(bookReadState.numericFlags, activeChar) {
                                    BookStateRepository.characterFlagsFor(bookReadState, activeChar)
                                }
                                if (flagsForChar.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color(0x40211044))
                                            .border(0.85.dp, sheetAccent.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                            .padding(14.dp)
                                    ) {
                                        Text(
                                            text = "${activeChar.displayName} hikâyeye yeni katıldı veya henüz doğrudan bir karar bayrağı tetiklenmedi. Önümüzdeki seçimlerin onunla olan bağını şekillendirecek.",
                                            fontFamily = InterFontFamily,
                                            color = MorCamTokens.LavenderLight,
                                            fontSize = 12.5.sp,
                                            lineHeight = 18.5.sp
                                        )
                                    }
                                } else {
                                    flagsForChar.forEach { (axis, value) ->
                                        val rowColor = if (axis.isNegativeTone || value < 0) Color(0xFFFCA5A5) else Color(axis.accentHex)
                                        val signText = if (value > 0) "+$value" else "$value"
                                        val fillFrac = (kotlin.math.abs(value) / 12f).coerceIn(0.14f, 1f)
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(rowColor.copy(alpha = 0.11f))
                                                .border(0.9.dp, rowColor.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                                                .padding(horizontal = 13.dp, vertical = 10.dp),
                                            verticalArrangement = Arrangement.spacedBy(7.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "${activeChar.possessiveName} ${axis.turkishLabel}",
                                                    fontFamily = InterFontFamily,
                                                    color = Color(0xFFFAF6FF),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(50))
                                                        .background(rowColor.copy(alpha = 0.22f))
                                                        .border(0.75.dp, rowColor.copy(alpha = 0.65f), RoundedCornerShape(50))
                                                        .padding(horizontal = 9.dp, vertical = 3.dp)
                                                ) {
                                                    Text(
                                                        text = signText,
                                                        style = noPadTextStyle,
                                                        fontFamily = InterFontFamily,
                                                        color = rowColor,
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                }
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(5.dp)
                                                    .clip(RoundedCornerShape(50))
                                                    .background(Color(0x33FFFFFF))
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth(fillFrac)
                                                        .fillMaxHeight()
                                                        .clip(RoundedCornerShape(50))
                                                        .background(rowColor)
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                val storyFlags = remember(bookReadState.numericFlags) {
                                    bookReadState.numericFlags.entries
                                        .filter { it.key.startsWith("story:") && it.value != 0 }
                                        .sortedByDescending { kotlin.math.abs(it.value) }
                                }
                                storyFlags.forEach { (key, value) ->
                                    val (label, colorHex) = BookStateRepository.describeFlagKey(key)
                                    val rowColor = if (value < 0) Color(0xFFFCA5A5) else Color(colorHex)
                                    val signText = if (value > 0) "+$value" else "$value"
                                    val fillFrac = (kotlin.math.abs(value) / 12f).coerceIn(0.14f, 1f)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(rowColor.copy(alpha = 0.11f))
                                            .border(0.9.dp, rowColor.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                                            .padding(horizontal = 13.dp, vertical = 10.dp),
                                        verticalArrangement = Arrangement.spacedBy(7.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = label,
                                                fontFamily = InterFontFamily,
                                                color = Color(0xFFFAF6FF),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(50))
                                                    .background(rowColor.copy(alpha = 0.22f))
                                                    .border(0.75.dp, rowColor.copy(alpha = 0.65f), RoundedCornerShape(50))
                                                    .padding(horizontal = 9.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = signText,
                                                    style = noPadTextStyle,
                                                    fontFamily = InterFontFamily,
                                                    color = rowColor,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(5.dp)
                                                .clip(RoundedCornerShape(50))
                                                .background(Color(0x33FFFFFF))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(fillFrac)
                                                    .fillMaxHeight()
                                                    .clip(RoundedCornerShape(50))
                                                    .background(rowColor)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
        }
    }
}
