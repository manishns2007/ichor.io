package com.causely.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import com.causely.domain.hypothesis.Hypothesis
import com.causely.ui.components.*
import com.causely.ui.theme.*

@Composable
fun HypothesesScreen(
    hypotheses: List<Hypothesis>,
    studentExplanation: String,
    onContinue: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    CauselyScaffold(
        stepLabel = "Competing Models",
        stepIndex = 3,
        totalSteps = 7
    ) {
        Text(
            "Competing Causal Explanations",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Based on your observable reasoning, Causely identified two candidate causal explanations consistent with your prediction.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        // Student's explanation echo
        if (studentExplanation.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceBase)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text("Your explanation:", style = MaterialTheme.typography.labelSmall, color = TextMuted, letterSpacing = 1.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "\"$studentExplanation\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Hypothesis cards
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 2 }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                hypotheses.forEachIndexed { index, hypothesis ->
                    HypothesisCard(hypothesis = hypothesis, index = index)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Callout
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CaselyCyan.copy(alpha = 0.07f))
                .border(1.dp, CaselyCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    "⚗️  We need an experiment",
                    style = MaterialTheme.typography.titleSmall,
                    color = CaselyCyan,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "These two models make different predictions. We need an experiment that clearly separates them.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        Spacer(Modifier.weight(1f))

        CauselyPrimaryButton(
            text = "Let's Test Them →",
            onClick = onContinue,
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}

@Composable
private fun HypothesisCard(hypothesis: Hypothesis, index: Int) {
    val isLinear = hypothesis.id == "linear_velocity_range"
    val color = if (isLinear) HypothesisLinearColor else HypothesisQuadraticColor
    val bg = if (isLinear) HypothesisLinearBg else HypothesisQuadraticBg
    val label = if (isLinear) "H1" else "H2"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(label, style = MaterialTheme.typography.labelLarge, color = color, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = hypothesis.formula,
                    style = MaterialTheme.typography.headlineMedium,
                    color = color,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = hypothesis.name,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = hypothesis.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}
