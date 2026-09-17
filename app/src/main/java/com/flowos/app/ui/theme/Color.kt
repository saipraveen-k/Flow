package com.flowos.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * FlowOS Flagship V3 Semantic Palette
 * Optimized for iQOO premium high-contrast AMOLED displays.
 */
val Background = Color(0xFF070707) // True deep black
val SurfacePrimary = Color(0xFF0E0E0E)
val SurfaceSecondary = Color(0xFF141414)
val SurfaceElevated = Color(0xFF1C1600) // Tinted dark surface
val SurfaceOutline = Color(0xFF242424)

// Flagship Accent: iQOO-inspired Kinetic Yellow
val FlowAccent = Color(0xFFFFD400) 
val FlowAccentDark = Color(0xFFB89600)
val FlowAccentGlow = Color(0xFFFFD400).copy(alpha = 0.15f)

// Premium Text
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF999999)
val TextMuted = Color(0xFF555555)
val TextOnAccent = Color(0xFF000000)

// Intelligence Semantics (State-driven)
val Success = Color(0xFF66FF00) // Electric Green (Verified)
val Warning = Color(0xFFFF9900) // Warning Orange (At Risk)
val Error = Color(0xFFFF0033)   // Critical Red (Friction)
val Info = Color(0xFF00CCFF)    // Intelligence Blue (Context)
val Neutral = Color(0xFF242424) // Planned/Idle

// Priority mapping
val PriorityHigh = Error
val PriorityMedium = Warning
val PriorityLow = Success
