package com.causely.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.causely.ui.components.CauselyGradientBrush
import com.causely.ui.components.SectionChip
import com.causely.ui.theme.*

@Composable
fun HomeScreen(
    onStartProjectileMotion: () -> Unit,
    onStartDemoMode: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundDeep, Color(0xFF0D1530), BackgroundDeep),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(72.dp))

            // ── Track badge ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CauselyVioletDim.copy(alpha = 0.35f))
                    .border(1.dp, CauselyViolet.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "Smart Education  ·  iQOO Hackathon 2026",
                    style = MaterialTheme.typography.labelMedium,
                    color = CauselyVioletBright,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(Modifier.height(32.dp))

            // ── Wordmark ─────────────────────────────────────────────────────
            Text(
                text = "CAUSELY",
                style = MaterialTheme.typography.displayMedium.copy(
                    letterSpacing = 10.sp,
                    fontWeight = FontWeight.Black,
                    brush = CauselyGradientBrush
                )
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "AI that experimentally tests\nhow you reason.",
                style = MaterialTheme.typography.headlineSmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp
            )

            Spacer(Modifier.weight(1f))

            // ── Concept card ─────────────────────────────────────────────────
            ConceptCard(
                title = "Projectile Motion",
                description = "What happens to range when velocity doubles?",
                difficulty = "Introductory Physics",
                onClick = onStartProjectileMotion
            )

            Spacer(Modifier.height(20.dp))

            // ── How it works ─────────────────────────────────────────────────
            HowItWorksRow()

            Spacer(Modifier.weight(1f))

            // ── Demo mode ────────────────────────────────────────────────────
            TextButton(
                onClick = onStartDemoMode,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "⚡  Run Hackathon Demo",
                    style = MaterialTheme.typography.labelLarge,
                    color = CauselyViolet
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ConceptCard(
    title: String,
    description: String,
    difficulty: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF1A2240),
                        Color(0xFF142035)
                    )
                )
            )
            .border(1.dp, CauselyViolet.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(24.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CauselyVioletDim.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = CauselyVioletBright,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = difficulty,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
                Spacer(Modifier.weight(1f))
                Text("→", color = CauselyViolet, fontSize = 24.sp)
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )

            Spacer(Modifier.height(16.dp))

            // Mini flow preview
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Predict", "Reason", "Hypothesize", "Experiment", "Discover").forEachIndexed { i, step ->
                    Text(
                        text = step,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (i == 0) CauselyVioletBright else TextMuted,
                        fontSize = 10.sp
                    )
                    if (i < 4) {
                        Text("→", color = TextMuted, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun HowItWorksRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(
            Triple("🔬", "Discover", "Not receive"),
            Triple("📊", "Evidence", "Not certainty"),
            Triple("🧪", "Transfer", "Confirmed")
        ).forEach { (icon, title, sub) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceBase)
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(icon, fontSize = 20.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = sub,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}
