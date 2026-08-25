package com.causely.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.causely.ui.theme.*

/** ─── Screen scaffold with dark background and top label ─────────────────── */
@Composable
fun CauselyScaffold(
    stepLabel: String,
    stepIndex: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDeep)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Progress indicator
            Spacer(Modifier.height(48.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stepLabel.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "$stepIndex / $totalSteps",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
            Spacer(Modifier.height(8.dp))
            // Thin progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(BorderSubtle)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(stepIndex.toFloat() / totalSteps)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(listOf(CauselyViolet, CaselyCyan))
                        )
                )
            }
            Spacer(Modifier.height(24.dp))
            content()
        }
    }
}

/** ─── Primary action button ─────────────────────────────────────────────── */
@Composable
fun CauselyPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CauselyViolet,
            contentColor = Color.White,
            disabledContainerColor = BorderSubtle,
            disabledContentColor = TextMuted
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/** ─── Secondary outlined button ─────────────────────────────────────────── */
@Composable
fun CauselySecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderBright),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = TextSecondary
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

/** ─── Selectable prediction / answer option card ────────────────────────── */
@Composable
fun OptionCard(
    label: String,
    subtitle: String = "",
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) CauselyViolet else BorderSubtle
    val bgColor = if (selected) CauselyVioletGlow else SurfaceBase

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (selected) CauselyVioletBright else TextPrimary,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(50))
                        .background(CauselyViolet),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

/** ─── Evidence support bar (NOT "student confidence") ──────────────────── */
@Composable
fun EvidenceSupportBar(
    label: String,
    formula: String,
    supportFraction: Double,  // 0.0 to 1.0
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedWidth by animateFloatAsState(
        targetValue = supportFraction.toFloat(),
        animationSpec = tween(durationMillis = 1000),
        label = "evidence_bar"
    )
    val pct = (supportFraction * 100).toInt()

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formula,
                    style = MaterialTheme.typography.labelMedium,
                    color = color
                )
            }
            Text(
                text = "$pct%",
                style = MaterialTheme.typography.headlineSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(BorderSubtle)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .background(color)
            )
        }
        Text(
            text = "EVIDENCE SUPPORT",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/** ─── Section label / chip ──────────────────────────────────────────────── */
@Composable
fun SectionChip(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CauselyViolet
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            letterSpacing = 1.sp
        )
    }
}

/** ─── Surface card wrapper ──────────────────────────────────────────────── */
@Composable
fun CauselyCard(
    modifier: Modifier = Modifier,
    borderColor: Color = BorderSubtle,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceBase)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(20.dp),
        content = content
    )
}

/** ─── Gradient text — for CAUSELY wordmark etc. ────────────────────────── */
val CauselyGradientBrush: Brush
    get() = Brush.horizontalGradient(listOf(CauselyVioletBright, CaselyCyan))
