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
    primary = Palette.EmeraldLight,
    onPrimary = Color.White,
    primaryContainer = Palette.EmeraldContainerLight,
    onPrimaryContainer = Color(0xFF04281E),
    secondary = Palette.GoldLight,
    onSecondary = Color.White,
    secondaryContainer = Palette.GoldContainerLight,
    onSecondaryContainer = Color(0xFF3B2A00),
    tertiary = Palette.RoseLight,
    onTertiary = Color.White,
    tertiaryContainer = Palette.RoseContainerLight,
    onTertiaryContainer = Color(0xFF410E0A),
    background = Palette.PaperLight,
    onBackground = Palette.InkLight,
    surface = Color.White,
    onSurface = Palette.InkLight,
    surfaceVariant = Palette.SurfaceVariantLight,
    onSurfaceVariant = Color(0xFF4A5650),
    outline = Color(0xFF86908A),
    outlineVariant = Color(0xFFE3DCC8),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFFBE4E1),
    onErrorContainer = Color(0xFF5B1410),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFDFBF6),
    surfaceContainer = Color(0xFFF7F3E8),
    surfaceContainerHigh = Palette.SurfaceVariantLight,
    surfaceContainerHighest = Color(0xFFECE6D5)
)

private val DarkColors = darkColorScheme(
    primary = Palette.EmeraldDark,
    onPrimary = Color(0xFF04281E),
    primaryContainer = Palette.EmeraldContainerDark,
    onPrimaryContainer = Color(0xFFCFF3E2),
    secondary = Palette.GoldDark,
    onSecondary = Color(0xFF3B2A00),
    secondaryContainer = Color(0xFF3F3010),
    onSecondaryContainer = Color(0xFFF6E7BF),
    tertiary = Color(0xFFF2B8B0),
    onTertiary = Color(0xFF410E0A),
    tertiaryContainer = Color(0xFF7A2E27),
    onTertiaryContainer = Palette.RoseContainerLight,
    background = Palette.PaperDark,
    onBackground = Palette.InkDark,
    surface = Palette.SurfaceDark,
    onSurface = Palette.InkDark,
    surfaceVariant = Palette.SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFB4C2BA),
    outline = Color(0xFF7F8D85),
    outlineVariant = Color(0xFF28403A),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF5C1713),
    onErrorContainer = Color(0xFFFFDAD6),
    surfaceContainerLowest = Color(0xFF050D0B),
    surfaceContainerLow = Color(0xFF0B1814),
    surfaceContainer = Color(0xFF10201A),
    surfaceContainerHigh = Palette.SurfaceVariantDark,
    surfaceContainerHighest = Color(0xFF1B332A)
)

/** আরও গোলাকার কোণ: নরম, দামি অনুভূতির জন্য। */
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
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
