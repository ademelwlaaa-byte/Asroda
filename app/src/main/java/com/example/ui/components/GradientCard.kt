package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val DefaultCardFillColors = listOf(Color(0xFF130C2A), Color(0xFF0E0820))
private val DefaultCardBorderColors = listOf(Color(0xFF4C1D95), Color(0xFF2E1065), Color(0xFF4C1D95))
private val ActiveCardFillColors = listOf(Color(0xFF1E0B3A), Color(0xFF120820))
private val WarningCardFillColors = listOf(Color(0xFF1C1000), Color(0xFF0F0A00))
private val WarningCardBorderColors = listOf(Color(0xFFF59E0B), Color(0xFF92400E), Color(0xFFF59E0B))
private val SuccessCardFillColors = listOf(Color(0xFF001A0E), Color(0xFF00100A))
private val SuccessCardBorderColors = listOf(Color(0xFF10B981), Color(0xFF065F46), Color(0xFF10B981))
private val DangerCardFillColors = listOf(Color(0xFF1A0000), Color(0xFF100000))
private val DangerCardBorderColors = listOf(Color(0xFFEF4444), Color(0xFF7F1D1D), Color(0xFFEF4444))

/**
 * Drop-in replacement for Card() with gradient fill + gradient border.
 * Caches shape and gradient brushes across recompositions.
 */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp,
    fillColors: List<Color> = DefaultCardFillColors,
    borderColors: List<Color> = DefaultCardBorderColors,
    borderWidth: Dp = 1.2.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }
    val fillBrush = remember(fillColors) { Brush.linearGradient(fillColors) }
    val borderBrush = remember(borderColors) { Brush.linearGradient(borderColors) }
    Box(
        modifier = modifier
            .clip(shape)
            .background(fillBrush)
            .border(
                width = borderWidth,
                brush = borderBrush,
                shape = shape
            ),
        content = content
    )
}

/** Active/selected card — brighter purple gradient */
@Composable
fun ActiveGradientCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp,
    accentColor: Color = Color(0xFF7C3AED),
    content: @Composable BoxScope.() -> Unit
) {
    val borderColors = remember(accentColor) {
        listOf(accentColor, accentColor.copy(alpha = 0.5f), accentColor)
    }
    GradientCard(
        modifier = modifier,
        cornerRadius = cornerRadius,
        fillColors = ActiveCardFillColors,
        borderColors = borderColors,
        borderWidth = 1.8.dp,
        content = content
    )
}

/** Warning/orange accented card */
@Composable
fun WarningGradientCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp,
    content: @Composable BoxScope.() -> Unit
) = GradientCard(
    modifier = modifier,
    cornerRadius = cornerRadius,
    fillColors = WarningCardFillColors,
    borderColors = WarningCardBorderColors,
    borderWidth = 1.5.dp,
    content = content
)

/** Success/green accented card */
@Composable
fun SuccessGradientCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp,
    content: @Composable BoxScope.() -> Unit
) = GradientCard(
    modifier = modifier,
    cornerRadius = cornerRadius,
    fillColors = SuccessCardFillColors,
    borderColors = SuccessCardBorderColors,
    borderWidth = 1.5.dp,
    content = content
)

/** Danger/red accented card */
@Composable
fun DangerGradientCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp,
    content: @Composable BoxScope.() -> Unit
) = GradientCard(
    modifier = modifier,
    cornerRadius = cornerRadius,
    fillColors = DangerCardFillColors,
    borderColors = DangerCardBorderColors,
    borderWidth = 1.5.dp,
    content = content
)
