package com.causely.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Primary palette — electric violet ──────────────────────────────────────
val CauselyViolet = Color(0xFF7B6EEA)
val CauselyVioletDim = Color(0xFF4A3FA8)
val CauselyVioletBright = Color(0xFF9B8FF0)
val CauselyVioletGlow = Color(0xFF7B6EEA).copy(alpha = 0.15f)

// ─── Secondary — electric cyan (data / experiment color) ────────────────────
val CaselyCyan = Color(0xFF00C8F0)
val CaselyCyanDim = Color(0xFF007A95)
val CaselyCyanGlow = Color(0xFF00C8F0).copy(alpha = 0.15f)

// ─── Tertiary — neon green (success / confirmation) ─────────────────────────
val CauselyGreen = Color(0xFF00E676)
val CauselyGreenDim = Color(0xFF00A152)

// ─── Warning / challenge color (linear hypothesis) ──────────────────────────
val CauselyOrange = Color(0xFFFF8A65)
val CauselyOrangeDim = Color(0xFFBF5B3C)

// ─── Error / negative ───────────────────────────────────────────────────────
val CauselyRed = Color(0xFFFF6B6B)

// ─── Background layers (dark premium laboratory) ────────────────────────────
val BackgroundDeep = Color(0xFF070C18)      // deepest background
val BackgroundBase = Color(0xFF0B1020)      // main background
val SurfaceBase = Color(0xFF101828)         // cards
val SurfaceElevated = Color(0xFF162035)     // elevated surfaces
val SurfaceCard = Color(0xFF192540)         // bright cards
val BorderSubtle = Color(0xFF1E2D45)        // dividers
val BorderBright = Color(0xFF2A3F5F)        // highlighted borders

// ─── Text hierarchy ─────────────────────────────────────────────────────────
val TextPrimary = Color(0xFFF0F4FF)         // main text
val TextSecondary = Color(0xFFB0BEC5)       // secondary text
val TextMuted = Color(0xFF6B7A99)           // captions, hints

// ─── Hypothesis-specific colors ─────────────────────────────────────────────
// H1 Linear: challenged (warm orange — the "wrong" one in this context)
val HypothesisLinearColor = CauselyOrange
val HypothesisLinearBg = CauselyOrangeDim.copy(alpha = 0.15f)

// H2 Quadratic: supported by evidence (electric cyan — the "correct" one)
val HypothesisQuadraticColor = CaselyCyan
val HypothesisQuadraticBg = CaselyCyanDim.copy(alpha = 0.15f)
