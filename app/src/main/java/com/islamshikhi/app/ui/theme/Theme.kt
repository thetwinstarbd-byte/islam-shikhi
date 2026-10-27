package com.islamshikhi.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.islamshikhi.app.domain.model.ThemeMode

private val LightColors = lightColorScheme(
    primary = Color(0xFF0F5132),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E7DD),
    onPrimaryContainer = Color(0xFF062A1A),
    secondary = Color(0xFF8A6D1F),
    background = Color(0xFFFAFAF5),
    surface = Color(0xFFFAFAF5)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7FD1A5),
    onPrimary = Color(0xFF00391F),
    primaryContainer = Color(0xFF1B4D35),
    onPrimaryContainer = Color(0xFFD1E7DD),
    secondary = Color(0xFFE0C36A),
    background = Color(0xFF101412),
    surface = Color(0xFF101412)
)

@Composable
fun IslamShikhiTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, density.fontScale * fontScale)
    ) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            content = content
        )
    }
}
