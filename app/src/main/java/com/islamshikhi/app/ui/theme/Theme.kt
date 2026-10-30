package com.islamshikhi.app.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.islamshikhi.app.domain.model.ThemeMode

private val LightColors = lightColorScheme(
    primary = Palette.IndigoLight,
    onPrimary = Color.White,
    primaryContainer = Palette.IndigoContainerLight,
    onPrimaryContainer = Color(0xFF0E1A47),
    secondary = Palette.GoldLight,
    onSecondary = Color.White,
    secondaryContainer = Palette.GoldContainerLight,
    onSecondaryContainer = Color(0xFF3B2600),
    tertiary = Palette.RoseLight,
    onTertiary = Color.White,
    tertiaryContainer = Palette.RoseContainerLight,
    onTertiaryContainer = Color(0xFF410E0A),
    background = Palette.PaperLight,
    onBackground = Palette.InkLight,
    surface = Color.White,
    onSurface = Palette.InkLight,
    surfaceVariant = Color(0xFFE8ECF5),
    onSurfaceVariant = Color(0xFF475069),
    outline = Color(0xFF7A849C),
    outlineVariant = Color(0xFFDCE1EE),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFFBE4E1),
    onErrorContainer = Color(0xFF5B1410),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8F9FD),
    surfaceContainer = Color(0xFFEFF2F9),
    surfaceContainerHigh = Color(0xFFE8ECF5),
    surfaceContainerHighest = Color(0xFFE1E6F1)
)

private val DarkColors = darkColorScheme(
    primary = Palette.IndigoDark,
    onPrimary = Color(0xFF0E1A47),
    primaryContainer = Color(0xFF2A3F85),
    onPrimaryContainer = Palette.IndigoContainerLight,
    secondary = Palette.GoldDark,
    onSecondary = Color(0xFF3B2600),
    secondaryContainer = Color(0xFF5A3C00),
    onSecondaryContainer = Palette.GoldContainerLight,
    tertiary = Color(0xFFF2B8B0),
    onTertiary = Color(0xFF410E0A),
    tertiaryContainer = Color(0xFF7A2E27),
    onTertiaryContainer = Palette.RoseContainerLight,
    background = Palette.PaperDark,
    onBackground = Palette.InkDark,
    surface = Palette.SurfaceDark,
    onSurface = Palette.InkDark,
    surfaceVariant = Color(0xFF1D2540),
    onSurfaceVariant = Color(0xFFB3BCD4),
    outline = Color(0xFF7C86A3),
    outlineVariant = Color(0xFF2B3557),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF5C1713),
    onErrorContainer = Color(0xFFFFDAD6),
    surfaceContainerLowest = Color(0xFF0A0E1C),
    surfaceContainerLow = Color(0xFF111730),
    surfaceContainer = Color(0xFF161D38),
    surfaceContainerHigh = Color(0xFF1D2540),
    surfaceContainerHighest = Color(0xFF252E4D)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
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
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * স্ট্যাটাস বারের আইকন গাঢ় (হালকা পটভূমির জন্য) নাকি সাদা (গাঢ় পটভূমির জন্য), তা ঠিক করে।
 * অ্যাপের থিম ফোনের থিমের চেয়ে আলাদা হতে পারে, তাই এটা নিজে ঠিক করতে হয়।
 */
@Composable
fun SystemBarIcons(darkIcons: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkIcons
        }
    }
}
