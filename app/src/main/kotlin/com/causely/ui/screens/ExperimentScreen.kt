package com.causely.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.causely.domain.experiment.ScoredExperiment
import com.causely.ui.components.*
import com.causely.ui.theme.*

@Composable
fun ExperimentScreen(
    selectedExperiment: ScoredExperiment?,
    onEnterLaboratory: () -> Unit
) {
    if (selectedExperiment == null) {
        Box(Modifier.fillMaxSize().background(BackgroundDeep), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = CauselyViolet)
        }
        return
    }

    val exp = selectedExperiment.experiment
    val scorePct = (selectedExperiment.separationScore * 100).toInt()

    CauselyScaffold(
        stepLabel = "Experiment Selection",
        stepIndex = 4,
        totalSteps = 7
    ) {
        Text(
            "The Best Distinguishing Experiment",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "The ExperimentSelector scored all candidate experiments and selected the one that best separates the two models.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(Modifier.height(20.dp))

        // ── Selected experiment card ───────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceCard)
                .border(2.dp, CauselyViolet.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SectionChip("Selected", color = CauselyGreen)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Separation: ",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextMuted
                        )
                        Text(
                            "$scorePct%",
                            style = MaterialTheme.typography.titleLarge,
                            color = CauselyGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Velocity change display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    VelocityBadge("${exp.baseVelocity.toInt()} m/s", CauselyViolet)
                    Spacer(Modifier.width(12.dp))
                    Text("→", color = TextSecondary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(12.dp))
                    VelocityBadge("${exp.targetVelocity.toInt()} m/s", CaselyCyan)
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    "Velocity ratio: ${"%.1f".format(exp.velocityRatio)}×  ·  Angle: ${exp.angleDeg.toInt()}°  ·  Gravity: ${exp.gravity} m/s²",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Predictions ───────────────────────────────────────────────────
        Text(
            "What each model predicts:",
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary
        )
        Spacer(Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            selectedExperiment.predictions.forEachIndexed { i, pred ->
                val isLinear = pred.hypothesis.id == "linear_velocity_range"
                val color = if (isLinear) HypothesisLinearColor else HypothesisQuadraticColor

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.copy(alpha = 0.08f))
                        .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            if (isLinear) "H1: ${pred.hypothesis.name}" else "H2: ${pred.hypothesis.name}",
                            style = MaterialTheme.typography.titleSmall,
                            color = color,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            pred.hypothesis.formula,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                    Text(
                        "${"%.1f".format(pred.predictedRatio)}× range",
                        style = MaterialTheme.typography.headlineSmall,
                        color = color,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Selection reason ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceBase)
                .padding(14.dp)
        ) {
            Text(
                selectedExperiment.selectionReason,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }

        Spacer(Modifier.weight(1f))

        CauselyPrimaryButton(
            text = "Enter the Laboratory →",
            onClick = onEnterLaboratory,
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}

@Composable
private fun VelocityBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
