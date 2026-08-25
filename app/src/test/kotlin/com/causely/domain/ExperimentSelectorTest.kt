package com.causely.domain

import com.causely.domain.experiment.ExperimentSelector
import com.causely.domain.hypothesis.HypothesisEngine
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ExperimentSelectorTest {

    private val hypotheses = HypothesisEngine.initialHypotheses()

    @Test
    fun `separation score for 2x velocity ratio is 0_5`() {
        // H1 predicts 2.0x, H2 predicts 4.0x
        // separation = |4-2|/4 = 0.5
        val exp = ExperimentSelector.defaultCandidates().find { it.id == "exp_20_40" }!!
        val scored = ExperimentSelector.scoreExperiment(hypotheses, exp)
        assertThat(scored.separationScore).isWithin(0.001).of(0.5)
    }

    @Test
    fun `separation score for 3x velocity ratio is 0_67`() {
        // H1 predicts 3.0x, H2 predicts 9.0x
        // separation = |9-3|/9 = 6/9 ≈ 0.667
        val exp = ExperimentSelector.defaultCandidates().find { it.id == "exp_10_30" }!!
        val scored = ExperimentSelector.scoreExperiment(hypotheses, exp)
        assertThat(scored.separationScore).isWithin(0.01).of(6.0 / 9.0)
    }

    @Test
    fun `separation score for 1_25x velocity ratio is lower than for 2x`() {
        val exp125 = ExperimentSelector.defaultCandidates().find { it.id == "exp_20_25" }!!
        val exp2x = ExperimentSelector.defaultCandidates().find { it.id == "exp_20_40" }!!
        val scored125 = ExperimentSelector.scoreExperiment(hypotheses, exp125)
        val scored2x = ExperimentSelector.scoreExperiment(hypotheses, exp2x)
        assertThat(scored125.separationScore).isLessThan(scored2x.separationScore)
    }

    @Test
    fun `selectBest picks 10-to-30 experiment as highest separation`() {
        val candidates = ExperimentSelector.defaultCandidates()
        val best = ExperimentSelector.selectBest(hypotheses, candidates)
        // exp_10_30 has vRatio=3 → separation=0.667, which beats vRatio=2 → separation=0.5
        assertThat(best.experiment.id).isEqualTo("exp_10_30")
    }

    @Test
    fun `selectBest separation score is greater than any 2x experiment score`() {
        val candidates = ExperimentSelector.defaultCandidates()
        val best = ExperimentSelector.selectBest(hypotheses, candidates)
        val twoXScore = ExperimentSelector.scoreExperiment(
            hypotheses,
            ExperimentSelector.defaultCandidates().find { it.id == "exp_20_40" }!!
        ).separationScore
        assertThat(best.separationScore).isGreaterThan(twoXScore)
    }

    @Test
    fun `scored experiment predictions list has one entry per hypothesis`() {
        val exp = ExperimentSelector.defaultCandidates().first()
        val scored = ExperimentSelector.scoreExperiment(hypotheses, exp)
        assertThat(scored.predictions).hasSize(hypotheses.size)
    }

    @Test
    fun `linear hypothesis prediction in scored experiment equals velocity ratio`() {
        val exp = ExperimentSelector.defaultCandidates().find { it.id == "exp_20_40" }!! // ratio=2
        val scored = ExperimentSelector.scoreExperiment(hypotheses, exp)
        val linearPred = scored.predictions.find { it.hypothesis.id == "linear_velocity_range" }!!
        assertThat(linearPred.predictedRatio).isWithin(0.001).of(2.0)
    }

    @Test
    fun `quadratic hypothesis prediction in scored experiment equals velocity ratio squared`() {
        val exp = ExperimentSelector.defaultCandidates().find { it.id == "exp_20_40" }!! // ratio=2
        val scored = ExperimentSelector.scoreExperiment(hypotheses, exp)
        val quadPred = scored.predictions.find { it.hypothesis.id == "quadratic_velocity_range" }!!
        assertThat(quadPred.predictedRatio).isWithin(0.001).of(4.0)
    }

    @Test
    fun `calculateSeparation with equal predictions returns zero`() {
        // If both hypotheses predict the same, separation = 0
        val mockPreds = hypotheses.map {
            com.causely.domain.experiment.HypothesisPrediction(it, 2.0)
        }
        assertThat(ExperimentSelector.calculateSeparation(mockPreds)).isWithin(0.001).of(0.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `selectBest throws with fewer than 2 hypotheses`() {
        ExperimentSelector.selectBest(
            listOf(HypothesisEngine.linearHypothesis),
            ExperimentSelector.defaultCandidates()
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `selectBest throws with empty candidates`() {
        ExperimentSelector.selectBest(hypotheses, emptyList())
    }
}
