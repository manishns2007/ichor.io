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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.causely.domain.evidence.EvidenceState
import com.causely.domain.evidence.EvidenceVerdict
import com.causely.ui.components.*
import com.causely.ui.theme.*

@Composable
fun EvidenceScreen(
    evidenceState: EvidenceState?,
    onContinue: () -> Unit
) {
    if (evidenceState == null) return

    var showBars by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { showBars = true }

    CauselyScaffold(
        stepLabel = "Model Update",
        stepIndex = 6,
        totalSteps = 7
    ) {
        Text(
            "Evidence Update",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "The experiment result updates the evidence support for each candidate model.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(Modifier.height(20.dp))

        // ── Observed ratio callout ─────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceCard)
                .border(1.dp, CauselyGreen.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Observed Range Ratio", style = MaterialTheme.typography.labelLarge, color = TextMuted, letterSpacing = 1.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "${"%.2f".format(evidenceState.observedRatio)}×",
                    style = MaterialTheme.typography.displaySmall,
                    color = CauselyGreen,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "at ${"%.1f".format(evidenceState.velocityRatio)}× velocity change",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Evidence support bars ─────────────────────────────────────────
        // IMPORTANT: these bars show EVIDENCE SUPPORT — NOT student confidence / belief
        Text(
            "Evidence Support for Each Model:",
            style = MaterialTheme.typography.titleSmall,
            color = TextSecondary,
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            evidenceState.updatedHypotheses.forEach { hypothesis ->
                val isLinear = hypothesis.id == "linear_velocity_range"
                val color = if (isLinear) HypothesisLinearColor else HypothesisQuadraticColor
                val support = if (showBars) hypothesis.evidenceSupport else 0.0

                AnimatedVisibility(
                    visible = showBars,
                    enter = fadeIn(tween(600)) + expandVertically(tween(600))
                ) {
                    EvidenceSupportBar(
                        label = if (isLinear) "H1: ${hypothesis.name}" else "H2: ${hypothesis.name}",
                        formula = hypothesis.formula,
                        supportFraction = hypothesis.evidenceSupport,
                        color = color
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Verdict banner ────────────────────────────────────────────────
        VerdictBanner(verdict = evidenceState.verdict)

        Spacer(Modifier.height(16.dp))

        // ── Explanation ───────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceBase)
                .padding(16.dp)
        ) {
            Text(
                text = evidenceState.explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        Spacer(Modifier.weight(1f))

        CauselyPrimaryButton(
            text = "Transfer Test →",
            onClick = onContinue,
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}

@Composable
private fun VerdictBanner(verdict: EvidenceVerdict) {
    val (color, text, emoji) = when (verdict) {
        EvidenceVerdict.STRONGLY_SUPPORTS_QUADRATIC ->
            Triple(HypothesisQuadraticColor, "Strong evidence supports the Quadratic model (R ∝ v²)", "⬆️")
        EvidenceVerdict.WEAKLY_SUPPORTS_QUADRATIC ->
            Triple(HypothesisQuadraticColor, "Evidence leans toward the Quadratic model (R ∝ v²)", "↗️")
        EvidenceVerdict.STRONGLY_SUPPORTS_LINEAR ->
            Triple(HypothesisLinearColor, "Strong evidence supports the Linear model (R ∝ v)", "⬆️")
        EvidenceVerdict.WEAKLY_SUPPORTS_LINEAR ->
            Triple(HypothesisLinearColor, "Evidence leans toward the Linear model (R ∝ v)", "↗️")
        EvidenceVerdict.INCONCLUSIVE ->
            Triple(TextMuted, "Evidence is inconclusive — try a larger velocity change", "↔️")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 20.sp)
            Spacer(Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
