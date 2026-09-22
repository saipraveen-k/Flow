package com.flowos.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.flowos.app.settings.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = FlowAccent,
    onPrimary = Color.Black,
    primaryContainer = DarkSurfacePrimary,
    onPrimaryContainer = Color.White,
    secondary = Info,
    onSecondary = Color.Black,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurfacePrimary,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceSecondary,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkBorder,
    error = Error,
    onError = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = FlowAccent,
    onPrimary = Color.Black,
    primaryContainer = LightSurfacePrimary,
    onPrimaryContainer = Color.Black,
    secondary = Info,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurfacePrimary,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceSecondary,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightBorder,
    error = Error,
    onError = Color.White,
)

val LocalDesignSystem = staticCompositionLocalOf { DesignTokens }

object FlowTheme {
    val tokens: DesignTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalDesignSystem.current
}

@Composable
fun FlowOSTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM_DEFAULT,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM_DEFAULT -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalDesignSystem provides DesignTokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = FlowTypography,
            shapes = FlowShapes,
            content = content
        )
    }
}
