package com.example.praktam_2417051069.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = lightColorScheme(
    primary = BluePrimary,
    secondary = BlueSecondary,
    background = WhiteBackground,
    surface = CardSurface,
    onPrimary = OnPrimaryText,
    error = RedDelete
)

@Composable
fun PrakTAM_2417051069Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}
