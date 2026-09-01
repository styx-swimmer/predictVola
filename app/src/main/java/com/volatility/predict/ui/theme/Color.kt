package com.volatility.predict.ui.theme

import androidx.compose.ui.graphics.Color

// Base Brand Colors
val DarkBackground = Color(0xFF0C1017)
val DarkSurface = Color(0xFF151C28)
val DarkSurfaceElevated = Color(0xFF1E2838)
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

// Accent Colors
val AccentCyan = Color(0xFF38BDF8)
val AccentPurple = Color(0xFFA855F7)

// Status Color Tokens for Volatility Comparison
// Condition 1: Implied < Predicted -> Yellowish / Amber
val VolatilityYellowBg = Color(0xFF2C2411)
val VolatilityYellowBorder = Color(0xFFEAB308)
val VolatilityYellowText = Color(0xFFFDE047)
val VolatilityYellowBadge = Color(0x33EAB308)

// Condition 2: Implied >= Predicted -> Greenish / Emerald
val VolatilityGreenBg = Color(0xFF0E2A1E)
val VolatilityGreenBorder = Color(0xFF10B981)
val VolatilityGreenText = Color(0xFF6EE7B7)
val VolatilityGreenBadge = Color(0x3310B981)
