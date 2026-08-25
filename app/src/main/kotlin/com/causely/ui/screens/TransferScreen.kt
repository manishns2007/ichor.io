package com.causely.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.causely.domain.transfer.TransferOption
import com.causely.domain.transfer.TransferProblem
import com.causely.ui.components.*
import com.causely.ui.theme.*

@Composable
fun TransferScreen(
    transferProblem: TransferProblem?,
    selectedAnswerId: String?,
    onSelectAnswer: (String) -> Unit,
    onSubmit: () -> Unit
) {
    if (transferProblem == null) return

    CauselyScaffold(
        stepLabel = "Transfer Test",
        stepIndex = 7,
        totalSteps = 7
    ) {
        // ── Intro ─────────────────────────────────────────────────────────
        SectionChip("Can you transfer your updated model?", color = CaselyCyan)
        Spacer(Modifier.height(16.dp))

        Text(
            "New Scenario",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        // ── Question card ─────────────────────────────────────────────────
        CauselyCard(borderColor = CaselyCyan.copy(alpha = 0.4f)) {
            Text(
                text = transferProblem.question,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Apply the model you discovered:",
            style = MaterialTheme.typography.labelLarge,
            color = TextSecondary
        )
        Spacer(Modifier.height(12.dp))

        // ── Answer options ────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            transferProblem.options.forEach { option ->
                OptionCard(
                    label = option.label,
                    selected = selectedAnswerId == option.id,
                    onClick = { onSelectAnswer(option.id) }
                )
            }
        }

        Spacer(Modifier.weight(1f))

        CauselyPrimaryButton(
            text = "Submit Answer →",
            onClick = onSubmit,
            enabled = selectedAnswerId != null,
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}
