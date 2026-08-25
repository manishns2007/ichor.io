package com.causely.domain.evidence

import com.causely.domain.hypothesis.Hypothesis
import com.causely.domain.hypothesis.HypothesisEngine
import kotlin.math.abs

/**
 * Verdict describing which model the experimental evidence supports.
 * Derived from comparing observed range ratio to hypothesis predictions.
 */
enum class EvidenceVerdict {
    STRONGLY_SUPPORTS_LINEAR,
    WEAKLY_SUPPORTS_LINEAR,
    INCONCLUSIVE,
    WEAKLY_SUPPORTS_QUADRATIC,
    STRONGLY_SUPPORTS_QUADRATIC
}

/**
 * Result of updating evidence after an experiment.
 *
 * @param updatedHypotheses  hypotheses with updated evidence support values (normalised to sum 1.0)
 * @param observedRatio      the experimentally observed range ratio (R₂/R₁)
 * @param velocityRatio      the velocity ratio used in the experiment (v₂/v₁)
 * @param verdict            which model the evidence supports
 * @param explanation        human-readable explanation of the result
 */
data class EvidenceState(
    val updatedHypotheses: List<Hypothesis>,
    val observedRatio: Double,
    val velocityRatio: Double,
    val verdict: EvidenceVerdict,
    val explanation: String
)

/**
 * Updates evidence support for all competing hypotheses based on experimental observation.
 *
 * The evidence support values represent HOW WELL EACH MODEL FITS THE DATA —
 * NOT the probability that the student holds any belief.
 * Causely does not claim to know what the student thinks.
 */
object EvidenceUpdater {

    /**
     * Update evidence support for all [hypotheses] given the [observedRatio] at [velocityRatio].
     *
     * @param hypotheses    current list of competing hypotheses
     * @param observedRatio observed R₂/R₁ from the physics simulation
     * @param velocityRatio v₂/v₁ used in the experiment
     * @return [EvidenceState] with updated hypotheses and verdict
     */
    fun update(
        hypotheses: List<Hypothesis>,
        observedRatio: Double,
        velocityRatio: Double
    ): EvidenceState {
        // Step 1: Update each hypothesis's evidence support by comparing its prediction to observed
        val updated = hypotheses.map { h ->
            val predicted = HypothesisEngine.predictRangeRatio(h, velocityRatio)
            HypothesisEngine.updateEvidence(h, predicted, observedRatio)
        }

        // Step 2: Normalise so evidence support values sum to 1.0 (for percentage display)
        val normalized = HypothesisEngine.normalizeEvidence(updated)

        // Step 3: Determine verdict by comparing observed to linear and quadratic predictions
        val verdict = determineVerdict(observedRatio, velocityRatio)

        // Step 4: Build human-readable explanation using deterministic text
        val explanation = buildExplanation(observedRatio, velocityRatio, verdict)

        return EvidenceState(
            updatedHypotheses = normalized,
            observedRatio = observedRatio,
            velocityRatio = velocityRatio,
            verdict = verdict,
            explanation = explanation
        )
    }

    private fun determineVerdict(observedRatio: Double, velocityRatio: Double): EvidenceVerdict {
        val linearPred = velocityRatio                    // H1: R ∝ v
        val quadPred = velocityRatio * velocityRatio      // H2: R ∝ v²

        val linearError = if (linearPred > 0) abs(observedRatio - linearPred) / linearPred else 1.0
        val quadError = if (quadPred > 0) abs(observedRatio - quadPred) / quadPred else 1.0

        return when {
            linearError < 0.05 && quadError > 0.25 -> EvidenceVerdict.STRONGLY_SUPPORTS_LINEAR
            linearError < quadError && linearError < 0.15 -> EvidenceVerdict.WEAKLY_SUPPORTS_LINEAR
            quadError < 0.05 && linearError > 0.25 -> EvidenceVerdict.STRONGLY_SUPPORTS_QUADRATIC
            quadError < linearError && quadError < 0.15 -> EvidenceVerdict.WEAKLY_SUPPORTS_QUADRATIC
            else -> EvidenceVerdict.INCONCLUSIVE
        }
    }

    private fun buildExplanation(
        observedRatio: Double,
        velocityRatio: Double,
        verdict: EvidenceVerdict
    ): String {
        val obs = "%.2f".format(observedRatio)
        val linear = "%.2f".format(velocityRatio)
        val quad = "%.2f".format(velocityRatio * velocityRatio)

        return when (verdict) {
            EvidenceVerdict.STRONGLY_SUPPORTS_QUADRATIC ->
                "The observed range ratio (${obs}×) is consistent with the quadratic model " +
                        "(${quad}×) and contradicts the linear model (${linear}×). " +
                        "This experiment strongly supports R ∝ v² at fixed launch angle and gravity."

            EvidenceVerdict.WEAKLY_SUPPORTS_QUADRATIC ->
                "The observed ratio (${obs}×) is closer to the quadratic prediction (${quad}×) " +
                        "than the linear prediction (${linear}×). Moderate evidence supports R ∝ v²."

            EvidenceVerdict.STRONGLY_SUPPORTS_LINEAR ->
                "The observed ratio (${obs}×) matches the linear model (${linear}×) and is " +
                        "inconsistent with the quadratic model (${quad}×). Evidence supports R ∝ v."

            EvidenceVerdict.WEAKLY_SUPPORTS_LINEAR ->
                "The observed ratio (${obs}×) is closer to the linear prediction (${linear}×). " +
                        "Moderate evidence supports R ∝ v."

            EvidenceVerdict.INCONCLUSIVE ->
                "The observed ratio (${obs}×) does not clearly distinguish between the linear " +
                        "(${linear}×) and quadratic (${quad}×) models. " +
                        "A larger velocity change would provide stronger evidence."
        }
    }
}
