package com.causely.domain

import com.causely.domain.hypothesis.HypothesisEngine
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.math.abs

class HypothesisEngineTest {

    @Test
    fun `linear hypothesis predicts range ratio equal to velocity ratio`() {
        val vRatio = 2.0
        val predicted = HypothesisEngine.predictRangeRatio(HypothesisEngine.linearHypothesis, vRatio)
        assertThat(predicted).isWithin(0.001).of(2.0)
    }

    @Test
    fun `quadratic hypothesis predicts range ratio equal to velocity ratio squared`() {
        val vRatio = 2.0
        val predicted = HypothesisEngine.predictRangeRatio(HypothesisEngine.quadraticHypothesis, vRatio)
        assertThat(predicted).isWithin(0.001).of(4.0)
    }

    @Test
    fun `linear prediction for 3x velocity ratio is 3x`() {
        val predicted = HypothesisEngine.predictRangeRatio(HypothesisEngine.linearHypothesis, 3.0)
        assertThat(predicted).isWithin(0.001).of(3.0)
    }

    @Test
    fun `quadratic prediction for 3x velocity ratio is 9x`() {
        val predicted = HypothesisEngine.predictRangeRatio(HypothesisEngine.quadraticHypothesis, 3.0)
        assertThat(predicted).isWithin(0.001).of(9.0)
    }

    @Test
    fun `initial hypotheses has two entries with equal evidence support`() {
        val hypotheses = HypothesisEngine.initialHypotheses()
        assertThat(hypotheses).hasSize(2)
        assertThat(hypotheses[0].evidenceSupport).isWithin(0.001).of(0.5)
        assertThat(hypotheses[1].evidenceSupport).isWithin(0.001).of(0.5)
    }

    @Test
    fun `update evidence gives high support to quadratic when observed is 4x and vRatio is 2`() {
        val quadratic = HypothesisEngine.quadraticHypothesis
        val predicted = 4.0  // quadratic predicts 2² = 4x
        val observed = 4.0   // experiment shows ~4x
        val updated = HypothesisEngine.updateEvidence(quadratic, predicted, observed)
        assertThat(updated.evidenceSupport).isWithin(0.01).of(1.0)  // perfect match
    }

    @Test
    fun `update evidence gives low support to linear when observed is 4x and predicted is 2x`() {
        val linear = HypothesisEngine.linearHypothesis
        val predicted = 2.0  // linear predicts 2x
        val observed = 4.0   // experiment shows ~4x
        val updated = HypothesisEngine.updateEvidence(linear, predicted, observed)
        // relError = |2-4|/4 = 0.5 → accuracy = 0.5
        assertThat(updated.evidenceSupport).isWithin(0.01).of(0.5)
    }

    @Test
    fun `normalise evidence sums to 1 after update`() {
        val linear = HypothesisEngine.updateEvidence(HypothesisEngine.linearHypothesis, 2.0, 4.0)
        val quadratic = HypothesisEngine.updateEvidence(HypothesisEngine.quadraticHypothesis, 4.0, 4.0)
        val normalized = HypothesisEngine.normalizeEvidence(listOf(linear, quadratic))
        val total = normalized.sumOf { it.evidenceSupport }
        assertThat(total).isWithin(0.001).of(1.0)
    }

    @Test
    fun `normalised quadratic support exceeds linear support when observed is quadratic`() {
        val linear = HypothesisEngine.updateEvidence(HypothesisEngine.linearHypothesis, 2.0, 4.0)
        val quadratic = HypothesisEngine.updateEvidence(HypothesisEngine.quadraticHypothesis, 4.0, 4.0)
        val normalized = HypothesisEngine.normalizeEvidence(listOf(linear, quadratic))
        val linearSupport = normalized.find { it.id == "linear_velocity_range" }!!.evidenceSupport
        val quadSupport = normalized.find { it.id == "quadratic_velocity_range" }!!.evidenceSupport
        assertThat(quadSupport).isGreaterThan(linearSupport)
    }
}
