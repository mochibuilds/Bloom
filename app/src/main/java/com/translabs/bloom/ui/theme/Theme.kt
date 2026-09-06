package com.translabs.bloom.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BloomPalette = lightColorScheme(
    primary = Color(0xFFB4256C),           // raspberry pink
    primaryContainer = Color(0xFFFFD8E4),
    onPrimaryContainer = Color(0xFF3E0021),
    secondary = Color(0xFF386928),         // leaf green 🌱
    secondaryContainer = Color(0xFFB8F19F),
    onSecondaryContainer = Color(0xFF052100),
    background = Color(0xFFFFF7FA),
    surface = Color(0xFFFFF7FA),
)

@Composable
fun BloomTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BloomPalette,
        typography = Typography,
        content = content,
    )
}