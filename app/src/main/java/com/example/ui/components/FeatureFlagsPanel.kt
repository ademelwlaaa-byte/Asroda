package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LiterarySerifFontFamily
import com.example.util.AppFeatureFlags

internal fun partitionFeatureToggles(
    toggles: List<AppFeatureFlags.ToggleSpec>
): Pair<List<AppFeatureFlags.ToggleSpec>, List<AppFeatureFlags.ToggleSpec>> {
    val deduped = toggles.distinctBy { it.key }
    return deduped.partition { !it.isExperimental }
}

/**
 * Özellik toggle paneli — Mor Cam tasarım sistemiyle yenilendi.
 * AppFeatureFlags.allToggles() listesini okur, kararlı + deneysel olarak gruplar.
 */
@Composable
fun FeatureFlagsPanel(
    onDismiss: () -> Unit
) {
    val (stable, experimental) = remember { partitionFeatureToggles(AppFeatureFlags.allToggles()) }

    MorCamDialogShell(
        onDismissRequest = onDismiss,
        title = "Özellik Merkezi",
        subtitle = "Değişiklikler anında geçerli olur ve kalıcı olarak saklanır.",
        icon = Icons.Filled.Tune
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 460.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (stable.isNotEmpty()) {
                Text(
                    text = "✦ Kararlı Özellikler",
                    fontFamily = LiterarySerifFontFamily,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MorCamTokens.LavenderSoft
                )
                stable.forEach { spec ->
                    key(spec.key) {
                        FeatureToggleRow(spec = spec, isExperimental = false)
                    }
                }
            }

            if (experimental.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                MorCamDivider()
                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Science,
                        contentDescription = null,
                        tint = MorCamTokens.LavenderSoft,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Deneysel Özellikler",
                        fontFamily = LiterarySerifFontFamily,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MorCamTokens.LavenderSoft
                    )
                }

                experimental.forEach { spec ->
                    key(spec.key) {
                        FeatureToggleRow(spec = spec, isExperimental = true)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        MorCamCapsuleButton(
            text = "Tamam",
            onClick = onDismiss,
            variant = MorCamButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth(),
            testTag = "feature_flags_close_button"
        )
    }
}

@Composable
private fun FeatureToggleRow(
    spec: AppFeatureFlags.ToggleSpec,
    isExperimental: Boolean = false
) {
    var checked by remember(spec.key) { mutableStateOf(spec.getter()) }

    MorCamSwitchRow(
        title = spec.label,
        subtitle = spec.description,
        checked = checked,
        onCheckedChange = { v ->
            checked = v
            spec.setter(v)
        },
        badge = if (isExperimental) "Beta" else null
    )
}
