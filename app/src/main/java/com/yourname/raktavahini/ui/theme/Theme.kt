package com.yourname.raktavahini.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val RaktaColorScheme = lightColorScheme(
    primary        = BloodRed,
    onPrimary      = White,
    primaryContainer = SoftRed,
    onPrimaryContainer = DeepRed,
    secondary      = DeepRed,
    background     = White,
    surface        = LightGray,
    onBackground   = DarkGray,
    onSurface      = DarkGray,
)

@Composable
fun RaktaVahiniTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RaktaColorScheme,
        typography = Typography(),
        content = content
    )
}
