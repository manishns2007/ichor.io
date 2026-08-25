package com.causely.bridge

import com.causely.domain.experiment.CandidateExperiment
import com.causely.domain.experiment.ScoredExperiment
import com.causely.domain.hypothesis.Hypothesis

/**
 * Office Kit Bridge — clean abstraction for optional laptop-side computation.
 *
 * Architecture:
 *   PHONE → OfficeKitBridge → LAPTOP → Deep computation → PHONE
 *
 * The phone-only Causely experience is COMPLETE without this bridge.
 * This interface allows connecting a real laptop bridge when the
 * iQOO Office Kit hardware becomes available at the hackathon.
 *
 * Potential laptop tasks:
 *   - Evaluate a much larger set of candidate experiments (hundreds instead of 5)
 *   - Run a larger on-device reasoning model
 *   - Generate additional experiment candidates from a physics knowledge base
 *   - Perform deeper analysis of student reasoning patterns over multiple sessions
 *
 * IMPORTANT: The app MUST NEVER depend on this bridge for the core demo.
 * All callers must handle isAvailable() == false gracefully.
 */
interface OfficeKitBridge {

    /** Whether the Office Kit (laptop) is currently reachable. */
    suspend fun isAvailable(): Boolean

    /**
     * Request the laptop to evaluate a larger set of candidate experiments.
     * Returns null if the bridge is unavailable — caller falls back to phone-side [ExperimentSelector].
     */
    suspend fun evaluateExperiments(
        hypotheses: List<Hypothesis>,
        candidates: List<CandidateExperiment>
    ): List<ScoredExperiment>?

    /**
     * Request the laptop's reasoning model to classify a student explanation.
     * Returns null if unavailable — caller falls back to [RuleBasedReasoningModel].
     */
    suspend fun classifyReasoning(explanation: String): String?
}

/**
 * Stub implementation — Office Kit not yet connected.
 *
 * Always signals unavailable, causing all callers to use the phone-side fallback.
 * Replace with a real TCP/BLE/USB bridge implementation when the iQOO Office Kit
 * hardware/API is available.
 *
 * DO NOT add fake responses here. The interface must be genuinely honest about availability.
 */
class StubOfficeKitBridge : OfficeKitBridge {

    override suspend fun isAvailable(): Boolean = false

    override suspend fun evaluateExperiments(
        hypotheses: List<Hypothesis>,
        candidates: List<CandidateExperiment>
    ): List<ScoredExperiment>? = null

    override suspend fun classifyReasoning(explanation: String): String? = null
}
