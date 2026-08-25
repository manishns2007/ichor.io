package com.causely.domain.experiment

import com.causely.domain.hypothesis.Hypothesis
import com.causely.domain.hypothesis.HypothesisEngine
import kotlin.math.abs

/**
 * A candidate experiment defined by two velocity conditions.
 *
 * @param id            unique identifier
 * @param baseVelocity  control condition velocity (m/s)
 * @param targetVelocity experimental condition velocity (m/s)
 * @param angleDeg      launch angle in degrees (held constant)
 * @param gravity       gravitational acceleration in m/s² (held constant)
 */
data class CandidateExperiment(
    val id: String,
    val baseVelocity: Double,
    val targetVelocity: Double,
    val angleDeg: Double = 45.0,
    val gravity: Double = 9.8
) {
    val velocityRatio: Double get() = targetVelocity / baseVelocity
    val description: String get() = "${baseVelocity.toInt()} → ${targetVelocity.toInt()} m/s"
}

/**
 * A hypothesis prediction for a specific experiment.
 */
data class HypothesisPrediction(
    val hypothesis: Hypothesis,
    val predictedRatio: Double
)

/**
 * A candidate experiment scored for its ability to distinguish competing hypotheses.
 *
 * @param experiment      the candidate experiment
 * @param predictions     what each hypothesis predicts for this experiment
 * @param separationScore normalised separation [0.0, 1.0] — higher means more distinguishing
 * @param selectionReason human-readable explanation of why this experiment was selected
 */
data class ScoredExperiment(
    val experiment: CandidateExperiment,
    val predictions: List<HypothesisPrediction>,
    val separationScore: Double,
    val selectionReason: String
)

/**
 * Selects the best experiment for distinguishing competing causal hypotheses.
 *
 * ALGORITHM (for each candidate experiment):
 *   1. vRatio = targetVelocity / baseVelocity
 *   2. For each hypothesis h: predicted[h] = h.predictionFunction(vRatio)
 *   3. separation = |pred_H1 − pred_H2| / max(pred_H1, pred_H2)
 *   4. Score the experiment by separation
 *
 * Select the experiment with the highest separation score.
 *
 * Example with linear (H1) vs quadratic (H2):
 *   Experiment 20→40 m/s (vRatio=2): H1=2.0×, H2=4.0×  → separation=0.50
 *   Experiment 20→25 m/s (vRatio=1.25): H1=1.25×, H2=1.56× → separation=0.20
 *   Experiment 10→30 m/s (vRatio=3): H1=3.0×, H2=9.0×  → separation=0.67 ← SELECTED
 *
 * This component is the TECHNICAL HERO of Causely.
 * It does NOT hard-code the "best experiment" — it actually scores all candidates.
 */
object ExperimentSelector {

    /**
     * Select the best experiment from [candidates] for distinguishing [hypotheses].
     *
     * @throws IllegalArgumentException if fewer than 2 hypotheses or no candidates
     */
    fun selectBest(
        hypotheses: List<Hypothesis>,
        candidates: List<CandidateExperiment>
    ): ScoredExperiment {
        require(hypotheses.size >= 2) { "Need at least 2 competing hypotheses, got ${hypotheses.size}" }
        require(candidates.isNotEmpty()) { "Need at least one candidate experiment" }

        return candidates
            .map { scoreExperiment(hypotheses, it) }
            .maxByOrNull { it.separationScore }!!
    }

    /**
     * Score a single experiment for its ability to distinguish the given hypotheses.
     */
    fun scoreExperiment(
        hypotheses: List<Hypothesis>,
        experiment: CandidateExperiment
    ): ScoredExperiment {
        val vRatio = experiment.velocityRatio
        val predictions = hypotheses.map { h ->
            HypothesisPrediction(
                hypothesis = h,
                predictedRatio = HypothesisEngine.predictRangeRatio(h, vRatio)
            )
        }
        val separation = calculateSeparation(predictions)
        val reason = buildSelectionReason(experiment, predictions, separation)
        return ScoredExperiment(experiment, predictions, separation, reason)
    }

    /**
     * Compute the normalised separation between hypothesis predictions.
     *
     * separation = |pred_max − pred_min| / pred_max
     *
     * Range [0, 1]:
     *   0.0 = predictions are identical (useless experiment)
     *   1.0 = one prediction is zero (maximum possible separation)
     */
    fun calculateSeparation(predictions: List<HypothesisPrediction>): Double {
        if (predictions.size < 2) return 0.0
        val values = predictions.map { it.predictedRatio }
        val maxPred = values.maxOrNull() ?: 1.0
        val minPred = values.minOrNull() ?: 0.0
        return if (maxPred > 0.0) (maxPred - minPred) / maxPred else 0.0
    }

    /**
     * Default candidate experiments for the projectile motion concept.
     * The selector will score all of these and pick the best.
     */
    fun defaultCandidates(): List<CandidateExperiment> = listOf(
        CandidateExperiment("exp_10_20", 10.0, 20.0),  // vRatio=2  → sep 0.50
        CandidateExperiment("exp_20_40", 20.0, 40.0),  // vRatio=2  → sep 0.50
        CandidateExperiment("exp_20_25", 20.0, 25.0),  // vRatio=1.25 → sep 0.20
        CandidateExperiment("exp_30_60", 30.0, 60.0),  // vRatio=2  → sep 0.50
        CandidateExperiment("exp_10_30", 10.0, 30.0),  // vRatio=3  → sep 0.67 ← expected winner
    )

    private fun buildSelectionReason(
        experiment: CandidateExperiment,
        predictions: List<HypothesisPrediction>,
        score: Double
    ): String {
        val predsStr = predictions.joinToString(", ") {
            "${it.hypothesis.name}: ${"%.2f".format(it.predictedRatio)}×"
        }
        val pct = "%.0f".format(score * 100)
        val vRatioStr = "%.1f".format(experiment.velocityRatio)
        return "At ${experiment.description} (velocity ratio ${vRatioStr}×), " +
                "the models predict distinct outcomes: $predsStr. " +
                "Separation score $pct% — this experiment best distinguishes the competing models."
    }
}
