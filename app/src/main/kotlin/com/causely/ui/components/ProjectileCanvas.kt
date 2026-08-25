package com.causely.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.causely.core.physics.ProjectileResult
import com.causely.ui.theme.BackgroundDeep
import com.causely.ui.theme.BorderSubtle
import com.causely.ui.theme.CaselyCyan
import com.causely.ui.theme.CauselyViolet
import com.causely.ui.theme.TextMuted

/**
 * Canvas-based interactive projectile simulation visual.
 *
 * Draws:
 *  - Subtle grid
 *  - Ground line
 *  - Base trajectory (violet) with range indicator
 *  - Comparison trajectory (cyan) with range indicator (when showComparison=true)
 *  - Animated projectile ball on base trajectory
 *  - Launch point marker
 *
 * All coordinates are computed from real [ProjectileResult] physics data.
 */
@Composable
fun ProjectileCanvas(
    baseResult: ProjectileResult?,
    comparisonResult: ProjectileResult?,
    animationProgress: Float,       // 0f = start, 1f = landed
    showComparison: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(BackgroundDeep)
    ) {
        if (baseResult == null) return@Canvas

        val w = size.width
        val h = size.height
        val groundY = h - 36f
        val paddingLeft = 40f
        val paddingTop = 20f
        val usableW = w - paddingLeft - 16f
        val usableH = groundY - paddingTop

        // Determine scale — fit both trajectories
        val allResults = listOfNotNull(baseResult, if (showComparison) comparisonResult else null)
        val maxRange = allResults.maxOf { it.range }.coerceAtLeast(1.0)
        val maxHeight = allResults.maxOf { it.maxHeight }.coerceAtLeast(1.0)

        val scaleX = (usableW / maxRange).toFloat()
        val scaleY = (usableH / maxHeight).toFloat()

        fun toCanvasX(xPhys: Double): Float = paddingLeft + (xPhys * scaleX).toFloat()
        fun toCanvasY(yPhys: Double): Float = groundY - (yPhys * scaleY).toFloat()

        // ── Grid lines ──────────────────────────────────────────────────────
        val gridColor = Color(0xFF151E30)
        for (i in 1..5) {
            val gx = paddingLeft + usableW * i / 5
            drawLine(gridColor, Offset(gx, paddingTop), Offset(gx, groundY), 1f)
        }
        for (i in 1..3) {
            val gy = groundY - usableH * i / 4
            drawLine(gridColor, Offset(paddingLeft, gy), Offset(w - 16f, gy), 1f)
        }

        // ── Ground line ──────────────────────────────────────────────────────
        drawLine(
            color = BorderSubtle,
            start = Offset(paddingLeft, groundY),
            end = Offset(w - 16f, groundY),
            strokeWidth = 2f
        )

        // ── Comparison trajectory (cyan) ─────────────────────────────────────
        if (showComparison && comparisonResult != null) {
            drawTrajectory(comparisonResult, ::toCanvasX, ::toCanvasY, CaselyCyan, 2.5f)
            // Range indicator
            drawRangeIndicator(comparisonResult.range, ::toCanvasX, groundY, CaselyCyan, 28f)
        }

        // ── Base trajectory (violet) ─────────────────────────────────────────
        drawTrajectory(baseResult, ::toCanvasX, ::toCanvasY, CauselyViolet, 3f)
        drawRangeIndicator(baseResult.range, ::toCanvasX, groundY, CauselyViolet, 20f)

        // ── Animated projectile ball ─────────────────────────────────────────
        if (baseResult.trajectoryPoints.isNotEmpty()) {
            val pts = baseResult.trajectoryPoints
            val idx = ((pts.size - 1) * animationProgress.coerceIn(0f, 1f)).toInt()
                .coerceIn(0, pts.size - 1)
            val pt = pts[idx]
            val bx = toCanvasX(pt.x)
            val by = toCanvasY(pt.y)

            // Glow effect
            drawCircle(CauselyViolet.copy(alpha = 0.25f), 22f, Offset(bx, by))
            drawCircle(CauselyViolet.copy(alpha = 0.5f), 14f, Offset(bx, by))
            drawCircle(Color.White, 7f, Offset(bx, by))
        }

        // ── Launch point ─────────────────────────────────────────────────────
        drawCircle(Color.White, 6f, Offset(toCanvasX(0.0), toCanvasY(0.0)))
        drawCircle(CauselyViolet.copy(alpha = 0.4f), 12f, Offset(toCanvasX(0.0), toCanvasY(0.0)))
    }
}

private fun DrawScope.drawTrajectory(
    result: ProjectileResult,
    toX: (Double) -> Float,
    toY: (Double) -> Float,
    color: Color,
    strokeWidth: Float
) {
    if (result.trajectoryPoints.isEmpty()) return
    val path = Path()
    result.trajectoryPoints.forEachIndexed { i, pt ->
        val cx = toX(pt.x)
        val cy = toY(pt.y)
        if (i == 0) path.moveTo(cx, cy) else path.lineTo(cx, cy)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawRangeIndicator(
    range: Double,
    toX: (Double) -> Float,
    groundY: Float,
    color: Color,
    yOffset: Float
) {
    if (range <= 0.0) return
    val y = groundY + yOffset
    drawLine(
        color = color.copy(alpha = 0.7f),
        start = Offset(toX(0.0), y),
        end = Offset(toX(range), y),
        strokeWidth = 3f,
        cap = StrokeCap.Round
    )
    // End cap
    drawCircle(color.copy(alpha = 0.7f), 4f, Offset(toX(range), y))
}
