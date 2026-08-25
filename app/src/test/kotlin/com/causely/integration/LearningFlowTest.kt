package com.causely.integration

import com.causely.core.physics.ProjectilePhysicsEngine
import com.causely.domain.evidence.EvidenceUpdater
import com.causely.domain.evidence.EvidenceVerdict
import com.causely.domain.experiment.ExperimentSelector
import com.causely.domain.hypothesis.HypothesisEngine
import com.causely.domain.reasoning.RuleBasedReasoningModel
import com.causely.domain.transfer.TransferEvaluator
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Integration test: complete hackathon demo flow using real engines.
 *
 * Flow: Prediction → Reasoning → Hypotheses → Experiment → Physics →
 *       Evidence Update → Transfer → CAUSAL TRANSFER CONFIRMED
 *
 * This validates the DEMO MODE contract.
 */
class LearningFlowTest {

    @Test
    fun `complete demo flow produces causal transfer confirmed`() = runTest {
        // === STEP 1: Student predicts 2x (linear misconception) ===
        val predictionAnswer = "2x"

        // === STEP 2: Student explains reasoning ===
        val explanation = "Because velocity and range increase proportionally."

        // === STEP 3: Classify observable reasoning ===
        val reasoningModel = RuleBasedReasoningModel()
        val classification = reasoningModel.classifyReasoning(explanation)
        assertThat(classification.dominantPattern.name).isEqualTo("LINEAR_PROPORTIONAL")

        // === STEP 4: Generate competing hypotheses ===
        val hypotheses = HypothesisEngine.fromReasoningPattern(classification.dominantPattern)
        assertThat(hypotheses).hasSize(2)

        // === STEP 5: Select best distinguishing experiment ===
        val candidates = ExperimentSelector.defaultCandidates()
        val selected = ExperimentSelector.selectBest(hypotheses, candidates)

        // Verify the experiment actually scores and selects (not hardcoded)
        assertThat(selected.separationScore).isGreaterThan(0.4)
        assertThat(selected.predictions).hasSize(2)

        val experiment = selected.experiment
        val vRatio = experiment.velocityRatio

        // === STEP 6: Run physics simulation (the "laboratory") ===
        val baseResult = ProjectilePhysicsEngine.calculate(
            experiment.baseVelocity, experiment.angleDeg, experiment.gravity
        )
        val expResult = ProjectilePhysicsEngine.calculate(
            experiment.targetVelocity, experiment.angleDeg, experiment.gravity
        )

        assertThat(baseResult.range).isGreaterThan(0.0)
        assertThat(expResult.range).isGreaterThan(baseResult.range)

        val observedRatio = expResult.range / baseResult.range

        // === STEP 7: Verify observed ratio matches quadratic prediction ===
        // Expected: observedRatio ≈ vRatio²
        val quadraticPrediction = vRatio * vRatio
        assertThat(observedRatio).isWithin(quadraticPrediction * 0.01).of(quadraticPrediction)

        // === STEP 8: Update evidence ===
        val evidenceState = EvidenceUpdater.update(hypotheses, observedRatio, vRatio)

        // Evidence must support quadratic
        assertThat(evidenceState.verdict).isAnyOf(
            EvidenceVerdict.STRONGLY_SUPPORTS_QUADRATIC,
            EvidenceVerdict.WEAKLY_SUPPORTS_QUADRATIC
        )

        // Quadratic hypothesis must have higher support than linear
        val linearSupport = evidenceState.updatedHypotheses
            .find { it.id == "linear_velocity_range" }!!.evidenceSupport
        val quadSupport = evidenceState.updatedHypotheses
            .find { it.id == "quadratic_velocity_range" }!!.evidenceSupport

        assertThat(quadSupport).isGreaterThan(linearSupport)

        // Evidence sums to 1
        val total = evidenceState.updatedHypotheses.sumOf { it.evidenceSupport }
        assertThat(total).isWithin(0.001).of(1.0)

        // === STEP 9: Transfer problem ===
        val transferProblem = TransferEvaluator.generateProblem(30.0, 60.0)
        val correctLabel = transferProblem.options.find { it.id == "correct" }!!.label
        assertThat(correctLabel).isEqualTo("4×")  // (60/30)² = 4

        // === STEP 10: Student answers correctly ===
        val transferResult = TransferEvaluator.evaluate(transferProblem, "correct")
        assertThat(transferResult.correct).isTrue()
        assertThat(transferResult.transferConfirmed).isTrue()
        assertThat(transferResult.feedback).contains("CAUSAL TRANSFER CONFIRMED")
    }

    @Test
    fun `all engines are deterministic — same inputs produce same outputs`() = runTest {
        val explanation = "velocity and range increase proportionally"

        // Run twice
        val model = RuleBasedReasoningModel()
        val c1 = model.classifyReasoning(explanation)
        val c2 = model.classifyReasoning(explanation)
        assertThat(c1.dominantPattern).isEqualTo(c2.dominantPattern)
        assertThat(c1.confidence).isEqualTo(c2.confidence)

        val hypotheses = HypothesisEngine.initialHypotheses()
        val candidates = ExperimentSelector.defaultCandidates()

        val s1 = ExperimentSelector.selectBest(hypotheses, candidates)
        val s2 = ExperimentSelector.selectBest(hypotheses, candidates)
        assertThat(s1.experiment.id).isEqualTo(s2.experiment.id)
        assertThat(s1.separationScore).isEqualTo(s2.separationScore)
    }
}
