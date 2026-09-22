package com.flowos.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * FlowOS Design System Tokens.
 */
object FlowDesignSystem {
    object Colors {
        val YellowAccent = Color(0xFFFFD400)
        val YellowAccentSoft = Color(0xFFFFD400).copy(alpha = 0.1f)
        
        val Success = Color(0xFF10B981)
        val Warning = Color(0xFFF59E0B)
        val Error = Color(0xFFEF4444)
        val Info = Color(0xFF3B82F6)
        
        val DarkBackground = Color(0xFF070707)
        val DarkSurface = Color(0xFF101010)
        val DarkSurfaceElevated = Color(0xFF161616)
        val DarkBorder = Color(0xFF282828)
        
        val LightBackground = Color(0xFFF7F8FA)
        val LightSurface = Color(0xFFFFFFFF)
        val LightSurfaceElevated = Color(0xFFF0F2F5)
        val LightBorder = Color(0xFFE6E9EE)
    }

    object Spacing {
        val None = 0.dp
        val Tiny = 4.dp
        val Small = 8.dp
        val Medium = 12.dp
        val Normal = 16.dp
        val Large = 20.dp
        val Huge = 24.dp
        val Section = 32.dp
        val Hero = 40.dp
    }

    object Shapes {
        val Small = 8.dp
        val Medium = 16.dp
        val Large = 24.dp
        val ExtraLarge = 32.dp
    }
}
