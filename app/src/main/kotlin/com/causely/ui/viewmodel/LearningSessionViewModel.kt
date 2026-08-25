package com.causely.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.causely.core.physics.ProjectilePhysicsEngine
import com.causely.core.physics.ProjectileResult
import com.causely.domain.evidence.EvidenceState
import com.causely.domain.evidence.EvidenceUpdater
import com.causely.domain.experiment.ExperimentSelector
import com.causely.domain.experiment.ScoredExperiment
import com.causely.domain.hypothesis.Hypothesis
import com.causely.domain.hypothesis.HypothesisEngine
import com.causely.domain.reasoning.ReasoningClassification
import com.causely.domain.reasoning.ReasoningModel
import com.causely.domain.reasoning.RuleBasedReasoningModel
import com.causely.domain.transfer.TransferEvaluator
import com.causely.domain.transfer.TransferProblem
import com.causely.domain.transfer.TransferResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The student's prediction about the velocity-range relationship. */
enum class PredictionAnswer(val label: String, val description: String) {
    TWO_X("2×", "Range doubles"),
    FOUR_X("4×", "Range quadruples"),
    NO_CHANGE("No change", "Range stays the same"),
    OTHER("Other", "Something else")
}

/**
 * Complete state for one learning session.
 * Drives all 9 screens via a single observable StateFlow.
 */
data class SessionState(
    // Prediction step
    val predictionAnswer: PredictionAnswer? = null,

    // Explanation step
    val explanationText: String = "",
    val isAnalyzingReasoning: Boolean = false,

    // Reasoning classification (from AI layer)
    val reasoningClassification: ReasoningClassification? = null,

    // Hypotheses step
    val hypotheses: List<Hypothesis> = emptyList(),

    // Experiment step
    val selectedExperiment: ScoredExperiment? = null,

    // Laboratory step
    val basePhysicsResult: ProjectileResult? = null,
    val experimentPhysicsResult: ProjectileResult? = null,
    val observedRatio: Double? = null,
    val laboratoryReady: Boolean = false,

    // Evidence step
    val evidenceState: EvidenceState? = null,

    // Transfer step
    val transferProblem: TransferProblem? = null,
    val transferResult: TransferResult? = null,

    // Meta
    val isDemoMode: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Master ViewModel for the Causely learning session.
 *
 * Responsible for:
 *  - Orchestrating the Prediction → Transfer flow
 *  - Delegating all computation to domain engines
 *  - Keeping UI state in a single [SessionState] StateFlow
 *
 * The AI layer ([ReasoningModel]) is injected and defaults to the
 * always-available [RuleBasedReasoningModel] fallback.
 */
class LearningSessionViewModel(
    private val reasoningModel: ReasoningModel = RuleBasedReasoningModel()
) : ViewModel() {

    private val _state = MutableStateFlow(SessionState())
    val state: StateFlow<SessionState> = _state.asStateFlow()

    // ─── Step 1: Prediction ────────────────────────────────────────────────

    fun selectPrediction(answer: PredictionAnswer) {
        _state.update { it.copy(predictionAnswer = answer) }
    }

    // ─── Step 2: Explanation input ─────────────────────────────────────────

    fun updateExplanation(text: String) {
        _state.update { it.copy(explanationText = text) }
    }

    /**
     * Classify the student's observable explanation and generate hypotheses.
     * Uses [ReasoningModel] (with deterministic fallback on error).
     */
    fun analyzeReasoning() {
        val text = _state.value.explanationText
        _state.update { it.copy(isAnalyzingReasoning = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val classification = try {
                    reasoningModel.classifyReasoning(text)
                } catch (e: Exception) {
                    // Fallback — app MUST NOT fail due to AI unavailability
                    RuleBasedReasoningModel().classifyReasoning(text)
                }

                val hypotheses = HypothesisEngine.fromReasoningPattern(classification.dominantPattern)
                val candidates = ExperimentSelector.defaultCandidates()
                val selected = ExperimentSelector.selectBest(hypotheses, candidates)

                _state.update {
                    it.copy(
                        isAnalyzingReasoning = false,
                        reasoningClassification = classification,
                        hypotheses = hypotheses,
                        selectedExperiment = selected
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isAnalyzingReasoning = false,
                        errorMessage = "Analysis failed: ${e.message}"
                    )
                }
            }
        }
    }

    // ─── Step 6: Laboratory — run the physics experiment ──────────────────

    /**
     * Run the physics simulation for the selected experiment.
     * Computes both the base and experimental conditions.
     * This uses the deterministic [ProjectilePhysicsEngine] — NOT the AI layer.
     */
    fun runExperiment() {
        val exp = _state.value.selectedExperiment?.experiment ?: return

        val baseResult = ProjectilePhysicsEngine.calculate(
            velocity = exp.baseVelocity,
            angleDeg = exp.angleDeg,
            gravity = exp.gravity
        )
        val expResult = ProjectilePhysicsEngine.calculate(
            velocity = exp.targetVelocity,
            angleDeg = exp.angleDeg,
            gravity = exp.gravity
        )
        val observedRatio = if (baseResult.range > 0.0) expResult.range / baseResult.range else 0.0

        _state.update {
            it.copy(
                basePhysicsResult = baseResult,
                experimentPhysicsResult = expResult,
                observedRatio = observedRatio,
                laboratoryReady = true
            )
        }
    }

    // ─── Step 7: Evidence update ───────────────────────────────────────────

    /**
     * Update evidence support for all hypotheses based on the observed ratio.
     * Also generates the transfer problem for the next step.
     */
    fun confirmObservation() {
        val currentState = _state.value
        val hypotheses = currentState.hypotheses
        val observedRatio = currentState.observedRatio ?: return
        val velocityRatio = currentState.selectedExperiment?.experiment?.velocityRatio ?: return

        val evidenceState = EvidenceUpdater.update(hypotheses, observedRatio, velocityRatio)
        val transferProblem = TransferEvaluator.generateProblem(30.0, 60.0)

        _state.update {
            it.copy(
                evidenceState = evidenceState,
                transferProblem = transferProblem
            )
        }
    }

    // ─── Step 8: Transfer ─────────────────────────────────────────────────

    fun submitTransferAnswer(answerId: String) {
        val problem = _state.value.transferProblem ?: return
        val result = TransferEvaluator.evaluate(problem, answerId)
        _state.update { it.copy(transferResult = result) }
    }

    // ─── Demo Mode ────────────────────────────────────────────────────────

    /**
     * Start Demo Mode — auto-fills the prediction and explanation with the
     * canonical hackathon demo values, then runs the full flow using REAL engines.
     * No results are faked or hardcoded.
     */
    fun startDemoMode() {
        _state.update {
            SessionState(
                isDemoMode = true,
                predictionAnswer = PredictionAnswer.TWO_X,
                explanationText = "Because velocity and range increase proportionally."
            )
        }
        analyzeReasoning()
    }

    // ─── Reset ────────────────────────────────────────────────────────────

    fun reset() {
        _state.update { SessionState() }
    }
}
