package com.causely.domain

import com.causely.domain.evidence.EvidenceUpdater
import com.causely.domain.evidence.EvidenceVerdict
import com.causely.domain.hypothesis.HypothesisEngine
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EvidenceUpdaterTest {

    private val hypotheses = HypothesisEngine.initialHypotheses()

    @Test
    fun `observed 4x at vRatio 2 produces strongly supports quadratic verdict`() {
        val state = EvidenceUpdater.update(hypotheses, observedRatio = 4.0, velocityRatio = 2.0)
        assertThat(state.verdict).isEqualTo(EvidenceVerdict.STRONGLY_SUPPORTS_QUADRATIC)
    }

    @Test
    fun `observed 2x at vRatio 2 produces strongly supports linear verdict`() {
        val state = EvidenceUpdater.update(hypotheses, observedRatio = 2.0, velocityRatio = 2.0)
        assertThat(state.verdict).isEqualTo(EvidenceVerdict.STRONGLY_SUPPORTS_LINEAR)
    }

    @Test
    fun `updated hypotheses sum to 1_0 after normalisation`() {
        val state = EvidenceUpdater.update(hypotheses, observedRatio = 4.0, velocityRatio = 2.0)
        val total = state.updatedHypotheses.sumOf { it.evidenceSupport }
        assertThat(total).isWithin(0.001).of(1.0)
    }

    @Test
    fun `quadratic support is higher than linear support when observed is quadratic`() {
        val state = EvidenceUpdater.update(hypotheses, observedRatio = 4.0, velocityRatio = 2.0)
        val linearSupport = state.updatedHypotheses
            .find { it.id == "linear_velocity_range" }!!.evidenceSupport
        val quadSupport = state.updatedHypotheses
            .find { it.id == "quadratic_velocity_range" }!!.evidenceSupport
        assertThat(quadSupport).isGreaterThan(linearSupport)
    }

    @Test
    fun `explanation contains observed ratio`() {
        val state = EvidenceUpdater.update(hypotheses, observedRatio = 4.0, velocityRatio = 2.0)
        assertThat(state.explanation).contains("4.00")
    }

    @Test
    fun `explanation for quadratic support mentions quadratic model`() {
        val state = EvidenceUpdater.update(hypotheses, observedRatio = 4.0, velocityRatio = 2.0)
        // Explanation must mention the quadratic model
        val lower = state.explanation.lowercase()
        assertThat(lower.contains("quadratic") || lower.contains("v²") || lower.contains("r ∝ v")).isTrue()
    }

    @Test
    fun `state carries correct observed ratio`() {
        val state = EvidenceUpdater.update(hypotheses, observedRatio = 3.95, velocityRatio = 2.0)
        assertThat(state.observedRatio).isWithin(0.001).of(3.95)
    }

    @Test
    fun `state carries correct velocity ratio`() {
        val state = EvidenceUpdater.update(hypotheses, observedRatio = 4.0, velocityRatio = 2.0)
        assertThat(state.velocityRatio).isWithin(0.001).of(2.0)
    }
}
