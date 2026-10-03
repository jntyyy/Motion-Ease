package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// FLAT DARK DESIGN TOKENS (Authoritative Reference Language)
// No gradients, no cyan. Pure flat dark gray surfaces with iOS Blue accent.
// =========================================================================

// Backgrounds & Surfaces (Flat, opaque, non-blur)
val FlatDarkBackground = Color(0xFF1F1F1F) // User requested #1F1F1F
val FlatCardSurface = Color(0xFF333336)     // Grouped Card Fill Dark
val CircleButtonBg = Color(0xFF353638)      // User requested #353638
val FlatCardSurfaceHover = Color(0xFF3A3A3E)
val FlatCardDivider = Color(0xFF48484A)     // Hairline inner divider

// Circular Header / Action Button Surface
val FlatHeaderButtonBg = Color(0x1FFFFFFF)  // White @ 8% opacity
val FlatHeaderButtonBorder = Color(0x1AFFFFFF)

// Accent Color (Single primary action color)
val IosAccentBlue = Color(0xFF0A84FF)       // iOS System Blue Dark
val IosAccentBluePressed = Color(0xFF0071E3)

// Text Hierarchy
val TextPrimary = Color(0xFFFFFFFF)        // High contrast primary text (100%)
val TextSecondary = Color(0xFF8E8E93)      // Secondary descriptive text / captions
val TextMuted = Color(0xFF636366)          // Muted labels / borders

// iOS-style Toggle Tokens
val ToggleTrackOff = Color(0xFF636366)     // Track when OFF
val ToggleKnobOff = Color(0xFF1C1C1E)      // Knob when OFF
val ToggleTrackOn = IosAccentBlue          // Track when ON (#0A84FF)
val ToggleKnobOn = Color(0xFFFFFFFF)       // Knob when ON

// Status Indicators (Subtle dots/badges only — never used as large backgrounds)
val StatusActiveGreen = Color(0xFF34D399)   // Active motion cues / Connected
val StatusWarningAmber = Color(0xFFFBBF24)  // Permission needed / Calibrating
val StatusErrorRed = Color(0xFFF87171)      // Sensors unavailable / Error
val StatusPausedBlue = Color(0xFF60A5FA)    // Motion cues paused

// Legacy compatibility aliases (Cleanly deprecated)
val DarkCharcoal = FlatDarkBackground
val Slate900 = FlatDarkBackground
val Slate800 = FlatCardSurface
val Slate700 = FlatCardDivider
val Slate600 = TextMuted
val Cyan80 = IosAccentBlue
val Cyan40 = IosAccentBlue
val OffWhite = TextPrimary
