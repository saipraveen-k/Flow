package com.flowos.app.ui.theme

import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object ColorTokens {
    val FlowBlue = Color(0xFF4F8CFF)
    val FlowBlueDark = Color(0xFF1E5BB8)
    val FlowIndigo = Color(0xFF6366F1)
    val FlowCyan = Color(0xFF06B6D4)
    val BackgroundDark = Color(0xFF0D1117)
    val SurfaceDarkPrimary = Color(0xFF161B22)
    val SurfaceDarkSecondary = Color(0xFF21262D)
    val SurfaceDarkElevated = Color(0xFF30363D)
    val SurfaceDarkOutline = Color(0xFF30363D)
    val TextDarkPrimary = Color(0xFFF0F6FC)
    val TextDarkSecondary = Color(0xFF8B949E)

    val BackgroundLight = Color(0xFFF8FAFC)
    val SurfaceLightPrimary = Color(0xFFFFFFFF)
    val SurfaceLightSecondary = Color(0xFFF1F5F9)
    val SurfaceLightElevated = Color(0xFFE2E8F0)
    val SurfaceLightOutline = Color(0xFFCBD5E1)
    val TextLightPrimary = Color(0xFF0F172A)
    val TextLightSecondary = Color(0xFF475569)

    val Success = Color(0xFF10B981)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFEF4444)
    val Info = Color(0xFF3B82F6)
}

object SpacingTokens {
    val None: Dp = 0.dp
    val ExtraSmall: Dp = 4.dp
    val Small: Dp = 8.dp
    val Medium: Dp = 16.dp
    val Large: Dp = 24.dp
    val ExtraLarge: Dp = 32.dp
    val Huge: Dp = 48.dp
}

object ShapeTokens {
    val Small = FlowShapes.small
    val Medium = FlowShapes.medium
    val Large = FlowShapes.large
    val Full = androidx.compose.foundation.shape.CircleShape
}

object ElevationTokens {
    val Level0: Dp = 0.dp
    val Level1: Dp = 2.dp
    val Level2: Dp = 4.dp
    val Level3: Dp = 8.dp
    val Level4: Dp = 12.dp
}

object TypographyTokens {
    val Typography: Typography = FlowTypography
}
