package com.flowos.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * FlowOS Premium Semantic Palette
 * Color communicates STATE, not just decoration.
 */
val Background = Color(0xFF080808)
val SurfacePrimary = Color(0xFF111111)
val SurfaceSecondary = Color(0xFF181818)
val SurfaceElevated = Color(0xFF202020)
val SurfaceOutline = Color(0xFF2A2A2A)

// Flagship Accent (Primary)
val FlowAccent = Color(0xFFFFD400) // iQOO Yellow
val FlowAccentDark = Color(0xFFC9A800)

// Text
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFA7A7A7)
val TextMuted = Color(0xFF707070)

// Semantic States
val Success = Color(0xFF70E000) // Verified / Completed
val Warning = Color(0xFFFFB000) // At Risk / Overestimate
val Error = Color(0xFFFF4D4D)   // Critical Friction / Collision
val Info = Color(0xFF4F8CFF)    // Context / Information
val Neutral = Color(0xFF2A2A2A) // Normal Task States

// Priority (Mapped to semantic where useful)
val PriorityHigh = Error
val PriorityMedium = Warning
val PriorityLow = Success
