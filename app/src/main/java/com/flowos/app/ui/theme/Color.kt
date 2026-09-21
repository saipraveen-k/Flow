package com.flowos.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * FlowOS V3 Flagship Color Tokens.
 * High-contrast, premium, and state-driven.
 */
val DarkBackground = Color(0xFF070707)
val LightBackground = Color(0xFFF5F5F5)

val DarkSurfacePrimary = Color(0xFF101010)
val LightSurfacePrimary = Color(0xFFFFFFFF)

val DarkSurfaceSecondary = Color(0xFF161616)
val LightSurfaceSecondary = Color(0xFFEBEBEB)

val DarkBorder = Color(0xFF282828)
val LightBorder = Color(0xFFDCDCDC)

// Flagship Accent: iQOO-inspired Kinetic Yellow
val FlowAccent = Color(0xFFFFD400) 
val FlowAccentDark = Color(0xFFB89600)
val FlowAccentSoft = Color(0xFFFFD400).copy(alpha = 0.1f)

// Semantic States
val Success = Color(0xFF66FF00) // Electric Green (Verified)
val Warning = Color(0xFFFF9900) // Warning Orange (At Risk)
val Error = Color(0xFFFF0033)   // Critical Red (Friction)
val Info = Color(0xFF00CCFF)    // Intelligence Blue (Context)
val Neutral = Color(0xFF555555) // Metadata / Planned

// Priority mapping (Legacy support)
val PriorityHigh = Error
val PriorityMedium = Warning
val PriorityLow = Success

// Text
val TextPrimaryDark = Color(0xFFFFFFFF)
val TextPrimaryLight = Color(0xFF000000)
val TextSecondaryDark = Color(0xFF999999)
val TextSecondaryLight = Color(0xFF666666)
val TextMuted = Color(0xFF555555)
