package com.causely.domain.reasoning

/**
 * Observable reasoning patterns that can be inferred from a student's
 * natural-language explanation.
 *
 * IMPORTANT: These are NOT psychological diagnoses.
 * They are candidate causal explanations consistent with the observable text.
 * Causely does not claim to read the student's mind.
 */
enum class ReasoningPattern(
    val displayName: String,
    val description: String
) {
    LINEAR_PROPORTIONAL(
        displayName = "Linear Proportional",
        description = "Observable reasoning: range increases proportionally with velocity (R ∝ v)"
    ),
    QUADRATIC_RELATIONSHIP(
        displayName = "Quadratic Relationship",
        description = "Observable reasoning: range increases with the square of velocity (R ∝ v²)"
    ),
    GENERIC_INCREASE(
        displayName = "Generic Increase",
        description = "Observable reasoning: range increases with velocity; precise relationship unspecified"
    ),
    UNKNOWN(
        displayName = "Unclassified",
        description = "Observable reasoning pattern could not be determined from the explanation"
    )
}

/**
 * Result of classifying a student's observable explanation into candidate reasoning patterns.
 *
 * @param dominantPattern   the most likely candidate explanation
 * @param confidence        classifier confidence [0.0, 1.0] — NOT certainty about student's mind
 * @param candidatePatterns all plausible candidate patterns in the explanation
 * @param classifiedBy      identifier of the model that produced this classification
 */
data class ReasoningClassification(
    val dominantPattern: ReasoningPattern,
    val confidence: Float,
    val candidatePatterns: List<ReasoningPattern>,
    val classifiedBy: String
)

/**
 * Interface for the observable-reasoning classification layer.
 *
 * Implementations:
 *   - [com.causely.domain.reasoning.RuleBasedReasoningModel] — deterministic fallback (always available)
 *   - LocalReasoningModel — on-device model (P1, plug in when available)
 *
 * The app MUST NEVER fail because an implementation is unavailable.
 * Always fall back to [RuleBasedReasoningModel] on any error.
 */
interface ReasoningModel {
    /**
     * Map a student's natural-language explanation to candidate reasoning patterns.
     * This is an observable-text classification task, not mind-reading.
     */
    suspend fun classifyReasoning(explanation: String): ReasoningClassification
}
