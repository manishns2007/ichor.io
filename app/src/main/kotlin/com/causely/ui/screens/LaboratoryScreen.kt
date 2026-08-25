package com.causely.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.causely.domain.experiment.ScoredExperiment
import com.causely.ui.components.*
import com.causely.ui.theme.*
import com.causely.ui.viewmodel.SimulationState

/**
 * The Laboratory screen — MOST IMPORTANT SCREEN in Causely.
 *
 * The student sees the phone act as a physics laboratory.
 * They can manipulate velocity, angle, and gravity.
 * They run the experiment and observe the range ratio.
 * The canvas shows both trajectories with live range indicators.
 * The comparison section shows H1 and H2 predictions vs observed.
 */
@Composable
fun LaboratoryScreen(
    selectedExperiment: ScoredExperiment?,
    simState: SimulationState,
    onVelocityChanged: (Float) -> Unit,
    onAngleChanged: (Float) -> Unit,
    onGravityChanged: (Float) -> Unit,
    onRunExperiment: () -> Unit,
    onConfirmObservation: () -> Unit
) {
    if (selectedExperiment == null) return

    val exp = selectedExperiment.experiment
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(48.dp))

            // ── Header ─────────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "LABORATORY",
                    style = MaterialTheme.typography.labelMedium,
                    color = CauselyGreen,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.weight(1f))
                SectionChip("5 / 7", color = TextMuted)
            }

            Spacer(Modifier.height(6.dp))
            Text(
                "Interactive Physics Experiment",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Experiment: ${exp.description}",
                style = MaterialTheme.typography.bodyMedium,
                color = CaselyCyan
            )

            Spacer(Modifier.height(16.dp))

            // ── Trajectory Canvas ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceBase)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                    .padding(4.dp)
            ) {
                ProjectileCanvas(
                    baseResult = simState.currentResult,
                    comparisonResult = simState.comparisonResult,
                    animationProgress = simState.animationProgress,
                    showComparison = simState.showComparison,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Canvas legend
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendDot(CauselyViolet)
                Spacer(Modifier.width(4.dp))
                Text("${exp.baseVelocity.toInt()} m/s", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                if (simState.showComparison) {
                    Spacer(Modifier.width(16.dp))
                    LegendDot(CaselyCyan)
                    Spacer(Modifier.width(4.dp))
                    Text("${exp.targetVelocity.toInt()} m/s", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Sliders ────────────────────────────────────────────────────
            Text("Adjust Parameters", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
            Spacer(Modifier.height(8.dp))

            PhysicsSlider(
                label = "Velocity",
                value = simState.velocity,
                min = 1f, max = 80f,
                unit = "m/s",
                color = CauselyViolet,
                onValueChange = onVelocityChanged
            )
            PhysicsSlider(
                label = "Launch Angle",
                value = simState.angleDeg,
                min = 5f, max = 85f,
                unit = "°",
                color = CaselyCyan,
                onValueChange = onAngleChanged
            )
            PhysicsSlider(
                label = "Gravity",
                value = simState.gravity,
                min = 1f, max = 20f,
                unit = "m/s²",
                color = CauselyOrange,
                onValueChange = onGravityChanged
            )

            Spacer(Modifier.height(16.dp))

            // ── Physics results summary ────────────────────────────────────
            simState.currentResult?.let { result ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PhysicsStat(
                        label = "Range",
                        value = "${"%.1f".format(result.range)} m",
                        color = CauselyViolet,
                        modifier = Modifier.weight(1f)
                    )
                    PhysicsStat(
                        label = "Max Height",
                        value = "${"%.1f".format(result.maxHeight)} m",
                        color = CaselyCyan,
                        modifier = Modifier.weight(1f)
                    )
                    PhysicsStat(
                        label = "Flight Time",
                        value = "${"%.2f".format(result.timeOfFlight)} s",
                        color = CauselyOrange,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Run Experiment button ──────────────────────────────────────
            if (!simState.experimentRan) {
                Button(
                    onClick = onRunExperiment,
                    enabled = !simState.isAnimating,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CauselyGreen,
                        contentColor = BackgroundDeep,
                        disabledContainerColor = BorderSubtle,
                        disabledContentColor = TextMuted
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (simState.isAnimating) "Running..." else "Run Experiment",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ── Comparison section (after experiment runs) ─────────────────
            if (simState.experimentRan && !simState.isAnimating) {
                Spacer(Modifier.height(16.dp))
                ComparisonSection(
                    selectedExperiment = selectedExperiment,
                    simState = simState
                )
                Spacer(Modifier.height(16.dp))
                CauselyPrimaryButton(
                    text = "Confirm Observation →",
                    onClick = onConfirmObservation
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ComparisonSection(
    selectedExperiment: ScoredExperiment,
    simState: SimulationState
) {
    val exp = selectedExperiment.experiment
    val baseRange = simState.currentResult?.range ?: 0.0
    val expRange = simState.comparisonResult?.range ?: 0.0
    val observedRatio = if (baseRange > 0) expRange / baseRange else 0.0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .border(1.dp, CauselyGreen.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column {
            Text(
                "Predictions vs Observation",
                style = MaterialTheme.typography.titleMedium,
                color = CauselyGreen,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(16.dp))

            // Three-column comparison
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // H1
                ComparisonCell(
                    label = "H1 PREDICTS",
                    value = "${"%.1f".format(exp.velocityRatio)}×",
                    subtitle = "Linear (R ∝ v)",
                    color = HypothesisLinearColor,
                    modifier = Modifier.weight(1f)
                )
                // H2
                ComparisonCell(
                    label = "H2 PREDICTS",
                    value = "${"%.1f".format(exp.velocityRatio * exp.velocityRatio)}×",
                    subtitle = "Quadratic (R ∝ v²)",
                    color = HypothesisQuadraticColor,
                    modifier = Modifier.weight(1f)
                )
                // Observed
                ComparisonCell(
                    label = "OBSERVED",
                    value = "${"%.1f".format(observedRatio)}×",
                    subtitle = "Physics engine",
                    color = CauselyGreen,
                    modifier = Modifier.weight(1f),
                    highlighted = true
                )
            }

            Spacer(Modifier.height(12.dp))

            // Range values
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text("${exp.baseVelocity.toInt()} m/s:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text("${"%.1f".format(baseRange)} m", style = MaterialTheme.typography.titleSmall, color = CauselyViolet, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${exp.targetVelocity.toInt()} m/s:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text("${"%.1f".format(expRange)} m", style = MaterialTheme.typography.titleSmall, color = CaselyCyan, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ComparisonCell(
    label: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (highlighted) color.copy(alpha = 0.15f) else BackgroundDeep)
            .border(
                width = if (highlighted) 2.dp else 1.dp,
                color = if (highlighted) color else color.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, letterSpacing = 0.5.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.headlineSmall, color = color, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextMuted, textAlign = TextAlign.Center)
    }
}

@Composable
private fun PhysicsSlider(
    label: String,
    value: Float,
    min: Float,
    max: Float,
    unit: String,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.width(70.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = min..max,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = BorderSubtle
            )
        )
        Text(
            "${"%.1f".format(value)} $unit",
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(64.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun PhysicsStat(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceBase)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LegendDot(color: Color) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(RoundedCornerShape(50))
            .background(color)
    )
}
