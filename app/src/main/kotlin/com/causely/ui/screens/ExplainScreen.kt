package com.causely.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.causely.ui.components.*
import com.causely.ui.theme.*
import com.causely.ui.viewmodel.PredictionAnswer

@Composable
fun ExplainScreen(
    prediction: PredictionAnswer?,
    explanationText: String,
    isAnalyzing: Boolean,
    onExplanationChanged: (String) -> Unit,
    onAnalyze: () -> Unit,
    onVoiceInput: () -> Unit  // P1 — gracefully no-ops if unavailable
) {
    CauselyScaffold(
        stepLabel = "Explain",
        stepIndex = 2,
        totalSteps = 7
    ) {
        // ── Echo prediction ───────────────────────────────────────────────
        prediction?.let {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CauselyVioletDim.copy(alpha = 0.2f))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text("Your prediction: ", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                Text(
                    it.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = CauselyVioletBright,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(20.dp))
        }

        // ── Prompt ────────────────────────────────────────────────────────
        Text(
            "Why do you think so?",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Describe your reasoning in plain language.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )

        Spacer(Modifier.height(20.dp))

        // ── Text input ────────────────────────────────────────────────────
        OutlinedTextField(
            value = explanationText,
            onValueChange = onExplanationChanged,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            placeholder = {
                Text(
                    "e.g. Because velocity and range increase proportionally...",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CauselyViolet,
                unfocusedBorderColor = BorderSubtle,
                focusedContainerColor = SurfaceBase,
                unfocusedContainerColor = SurfaceBase,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = CauselyViolet
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
            maxLines = 6
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${explanationText.length} characters",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            // Voice input button (P1 — shows button, graceful no-op if permissions unavailable)
            TextButton(
                onClick = onVoiceInput,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = "Voice input",
                    tint = CauselyViolet,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Voice", style = MaterialTheme.typography.labelMedium, color = CauselyViolet)
            }
        }

        Spacer(Modifier.weight(1f))

        // ── Analyze button ────────────────────────────────────────────────
        if (isAnalyzing) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = CauselyViolet, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Identifying candidate reasoning patterns...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }
        } else {
            CauselyPrimaryButton(
                text = "Analyze My Reasoning →",
                onClick = onAnalyze,
                enabled = explanationText.trim().isNotBlank(),
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}
