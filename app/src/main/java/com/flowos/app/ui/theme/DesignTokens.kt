package com.flowos.app.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * FlowOS Design System Tokens.
 * Centralized source of truth for colors, spacing, shapes and elevation.
 */
object DesignTokens {
    object Colors {
        // Flagship Accent: iQOO-inspired Kinetic Yellow
        val YellowAccent = Color(0xFFFFD400)
        val YellowAccentSoft = Color(0xFFFFD400).copy(alpha = 0.1f)
        val YellowAccentMuted = Color(0xFFB89600)
        
        // Semantic States
        val Success = Color(0xFF10B981)
        val Warning = Color(0xFFF59E0B)
        val Error = Color(0xFFEF4444)
        val Info = Color(0xFF3B82F6)
        
        // Dark Theme Surfaces
        val DarkBackground = Color(0xFF070707)
        val DarkSurfacePrimary = Color(0xFF101010)
        val DarkSurfaceSecondary = Color(0xFF161616)
        val DarkSurfaceTertiary = Color(0xFF202020)
        val DarkSurfaceGlass = Color(0xFFFFFFFF).copy(alpha = 0.05f)
        val DarkBorder = Color(0xFF282828)
        
        // Light Theme Surfaces
        val LightBackground = Color(0xFFF7F8FA)
        val LightSurfacePrimary = Color(0xFFFFFFFF)
        val LightSurfaceSecondary = Color(0xFFF0F2F5)
        val LightSurfaceTertiary = Color(0xFFE6E9EE)
        val LightSurfaceGlass = Color(0xFF000000).copy(alpha = 0.03f)
        val LightBorder = Color(0xFFE6E9EE)
    }

    object Spacing {
        val None: Dp = 0.dp
        val Tiny: Dp = 4.dp
        val Small: Dp = 8.dp
        val Medium: Dp = 12.dp
        val Normal: Dp = 16.dp
        val Large: Dp = 20.dp
        val Huge: Dp = 24.dp
        val Section: Dp = 32.dp
        val Hero: Dp = 40.dp
        val Massive: Dp = 48.dp
    }

    object Shapes {
        val Small = 8.dp
        val Medium = 16.dp
        val Large = 24.dp
        val ExtraLarge = 32.dp
        val Full = 999.dp
    }

    object Elevation {
        val Level0 = 0.dp
        val Level1 = 2.dp
        val Level2 = 4.dp
        val Level3 = 8.dp
    }

    object Animation {
        val Fast = 150
        val Normal = 300
        val Slow = 500
        
        val DefaultEasing = FastOutSlowInEasing
    }
}
