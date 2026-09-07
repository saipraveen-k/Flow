package com.flowos.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FlowDarkColors = darkColorScheme(
    primary = FlowAccent,
    onPrimary = Background,
    primaryContainer = SurfaceElevated,
    onPrimaryContainer = TextPrimary,
    secondary = FlowAccentDark,
    onSecondary = Background,
    background = Background,
    onBackground = TextPrimary,
    surface = SurfacePrimary,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceSecondary,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceOutline,
    error = Error,
    onError = TextPrimary,
)

private val FlowLightColors = lightColorScheme(
    primary = FlowAccentDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF0F0F0),
    onPrimaryContainer = Color.Black,
    secondary = FlowAccent,
    onSecondary = Color.Black,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color.DarkGray,
    outline = Color.LightGray,
    error = Error,
    onError = Color.White,
)

@Composable
fun FlowOSTheme(
    darkTheme: Boolean = true, // Default to dark flagship experience
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) FlowDarkColors else FlowLightColors
    
    MaterialTheme(
        colorScheme = colors,
        typography = FlowTypography,
        shapes = FlowShapes,
        content = content,
    )
}
