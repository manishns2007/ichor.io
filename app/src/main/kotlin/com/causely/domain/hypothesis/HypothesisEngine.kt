package com.causely.domain.hypothesis

import com.causely.domain.reasoning.ReasoningPattern
import kotlin.math.abs

/**
 * A candidate causal explanation for the observed relationship between
 * launch velocity and projectile range.
 *
 * These are NOT claims about what the student believes or thinks.
 * They are candidate causal explanations consistent with observable reasoning.
 *
 * @param id                    unique identifier
 * @param name                  short display name
 * @param formula               mathematical formula string
 * @param description           plain-language description
 * @param evidenceSupport       fraction of evidence support after experiment [0.0, 1.0]
 *                              This represents EVIDENCE SUPPORT, NOT student confidence.
 * @param predictionFunction    given a velocity ratio vR = v₂/v₁, returns predicted range ratio
 */
data class Hypothesis(
    val id: String,
    val name: String,
    val formula: String,
    val description: String,
    val evidenceSupport: Double = 0.5,
    val predictionFunction: (velocityRatio: Double) -> Double
)

/**
 * Generates and manages competing causal hypotheses for the velocity-range relationship.
 *
 * For the projectile motion MVP:
 *   H1 (Linear):    R ∝ v     — range doubles when velocity doubles
 *   H2 (Quadratic): R ∝ v²    — range quadruples when velocity doubles (at fixed θ, g)
 *
 * The quadratic relationship follows directly from R = v²·sin(2θ)/g.
 */
object HypothesisEngine {

    /**
     * H1: Linear candidate causal explanation.
     * Prediction: range ratio = velocity ratio.
     * Common misconception — students often reason proportionally.
     */
    val linearHypothesis = Hypothesis(
        id = "linear_velocity_range",
        name = "Linear Relationship",
        formula = "R ∝ v",
        description = "Range increases proportionally with velocity. Doubling velocity → doubling range.",
        evidenceSupport = 0.5,
        predictionFunction = { vRatio -> vRatio }
    )

    /**
     * H2: Quadratic candidate causal explanation.
     * Prediction: range ratio = velocity ratio squared.
     * Correct model: follows from R = v²·sin(2θ)/g at fixed angle and gravity.
     */
    val quadraticHypothesis = Hypothesis(
        id = "quadratic_velocity_range",
        name = "Quadratic Relationship",
        formula = "R ∝ v²",
        description = "Range increases with the square of velocity. " +
                "Doubling velocity → 4× range (at fixed launch angle and gravity).",
        evidenceSupport = 0.5,
        predictionFunction = { vRatio -> vRatio * vRatio }
    )

    /** Return the initial set of competing hypotheses with equal prior evidence support. */
    fun initialHypotheses(): List<Hypothesis> = listOf(linearHypothesis, quadraticHypothesis)

    /**
     * Generate competing hypotheses informed by the dominant observable reasoning pattern.
     * Both hypotheses are always generated; the pattern determines initial framing only.
     */
    fun fromReasoningPattern(pattern: ReasoningPattern): List<Hypothesis> {
        // Always return both competing hypotheses.
        // The student's pattern is used for narrative framing in the UI, not to filter hypotheses.
        return initialHypotheses()
    }

    /** Compute the predicted range ratio for a hypothesis given a velocity ratio. */
    fun predictRangeRatio(hypothesis: Hypothesis, velocityRatio: Double): Double =
        hypothesis.predictionFunction(velocityRatio)

    /**
     * Update evidence support for a hypothesis based on how well it predicted the observed ratio.
     *
     * Uses relative error: accuracy = 1 − |predicted − observed| / observed
     * The result is clamped to [0.0, 1.0].
     *
     * This represents EVIDENCE SUPPORT after experiment, NOT psychological certainty.
     */
    fun updateEvidence(
        hypothesis: Hypothesis,
        predictedRatio: Double,
        observedRatio: Double
    ): Hypothesis {
        if (predictedRatio <= 0.0 || observedRatio <= 0.0) {
            return hypothesis.copy(evidenceSupport = 0.0)
        }
        val relError = abs(predictedRatio - observedRatio) / observedRatio
        val accuracy = (1.0 - relError).coerceIn(0.0, 1.0)
        return hypothesis.copy(evidenceSupport = accuracy)
    }

    /**
     * Normalise evidence support values so they sum to 1.0.
     * This allows percentage display in the evidence bars.
     */
    fun normalizeEvidence(hypotheses: List<Hypothesis>): List<Hypothesis> {
        val total = hypotheses.sumOf { it.evidenceSupport }
        return if (total > 0.0) {
            hypotheses.map { it.copy(evidenceSupport = it.evidenceSupport / total) }
        } else {
            hypotheses
        }
    }
}
