package com.causely.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.causely.domain.transfer.TransferResult
import com.causely.ui.components.*
import com.causely.ui.theme.*

@Composable
fun ResultScreen(
    transferResult: TransferResult?,
    onStartAgain: () -> Unit
) {
    if (transferResult == null) return

    val scrollState = rememberScrollState()
    val confirmed = transferResult.transferConfirmed

    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { showContent = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500)) + scaleIn(tween(500))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // ── Result badge ──────────────────────────────────────────
                    if (confirmed) {
                        // Success state
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.radialGradient(
                                        listOf(CauselyGreen.copy(alpha = 0.3f), CauselyGreen.copy(alpha = 0.05f))
                                    )
                                )
                                .border(3.dp, CauselyGreen.copy(alpha = 0.7f), RoundedCornerShape(50)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✓", fontSize = 48.sp, color = CauselyGreen)
                        }
                        Spacer(Modifier.height(24.dp))
                        Text(
                            "CAUSAL TRANSFER",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextMuted,
                            letterSpacing = 4.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "CONFIRMED",
                            style = MaterialTheme.typography.displaySmall.copy(
                                brush = Brush.horizontalGradient(listOf(CauselyGreen, CaselyCyan))
                            ),
                            fontWeight = FontWeight.Black,
                            letterSpacing = 6.sp,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // Retry state
                        Text("🔬", fontSize = 64.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Not Quite",
                            style = MaterialTheme.typography.displaySmall,
                            color = CauselyOrange,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── Feedback ───────────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (confirmed) CauselyGreen.copy(alpha = 0.07f) else CauselyOrange.copy(alpha = 0.07f))
                            .border(
                                1.dp,
                                if (confirmed) CauselyGreen.copy(alpha = 0.4f) else CauselyOrange.copy(alpha = 0.4f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(20.dp)
                    ) {
                        Text(
                            text = transferResult.feedback,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (confirmed) CauselyGreen else CauselyOrange,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (confirmed) {
                        Spacer(Modifier.height(24.dp))

                        // ── Learning journey summary ───────────────────────────
                        Text(
                            "LEARNING JOURNEY",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextMuted,
                            letterSpacing = 2.sp
                        )
                        Spacer(Modifier.height(12.dp))

                        LearningJourneySummary()
                    }

                    Spacer(Modifier.height(32.dp))

                    // ── Actions ────────────────────────────────────────────────
                    CauselyPrimaryButton(
                        text = if (confirmed) "Explore Again" else "Try Again",
                        onClick = onStartAgain
                    )

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun LearningJourneySummary() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceBase)
            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        JourneyStep(
            icon = "🎯",
            label = "Initial Model",
            value = "R ∝ v  (Linear)",
            color = HypothesisLinearColor
        )
        HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
        JourneyStep(
            icon = "🧪",
            label = "Experiment",
            value = "Physics simulation",
            color = CauselyViolet
        )
        HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
        JourneyStep(
            icon = "📊",
            label = "Evidence",
            value = "Supported Quadratic",
            color = HypothesisQuadraticColor
        )
        HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
        JourneyStep(
            icon = "✓",
            label = "Updated Model",
            value = "R ∝ v²  (Quadratic)",
            color = CauselyGreen
        )
        HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
        JourneyStep(
            icon = "🚀",
            label = "Transfer",
            value = "Confirmed ✓",
            color = CauselyGreen
        )
    }
}

@Composable
private fun JourneyStep(icon: String, label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 18.sp)
            Spacer(Modifier.width(10.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        Text(value, style = MaterialTheme.typography.titleSmall, color = color, fontWeight = FontWeight.SemiBold)
    }
}
