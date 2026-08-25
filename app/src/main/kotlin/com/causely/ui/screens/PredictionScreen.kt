package com.causely.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.causely.ui.components.*
import com.causely.ui.theme.*
import com.causely.ui.viewmodel.PredictionAnswer

@Composable
fun PredictionScreen(
    selectedAnswer: PredictionAnswer?,
    onSelectAnswer: (PredictionAnswer) -> Unit,
    onContinue: () -> Unit
) {
    CauselyScaffold(
        stepLabel = "Predict",
        stepIndex = 1,
        totalSteps = 7
    ) {
        // ── Question ──────────────────────────────────────────────────────
        CauselyCard(borderColor = CauselyViolet.copy(alpha = 0.35f)) {
            SectionChip("Projectile Motion", color = CauselyViolet)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "If we double the launch velocity while keeping everything else constant, what happens to the range?",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Same angle · Same gravity · Velocity doubled",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }

        Spacer(Modifier.height(24.dp))

        // ── Options ───────────────────────────────────────────────────────
        Text(
            "Choose your prediction:",
            style = MaterialTheme.typography.labelLarge,
            color = TextSecondary
        )

        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PredictionAnswer.entries.forEach { answer ->
                OptionCard(
                    label = answer.label,
                    subtitle = answer.description,
                    selected = selectedAnswer == answer,
                    onClick = { onSelectAnswer(answer) }
                )
            }
        }

        Spacer(Modifier.weight(1f))

        CauselyPrimaryButton(
            text = "Continue →",
            onClick = onContinue,
            enabled = selectedAnswer != null,
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}
