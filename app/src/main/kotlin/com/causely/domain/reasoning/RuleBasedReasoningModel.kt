package com.causely.domain.reasoning

/**
 * Deterministic rule-based reasoning classifier.
 *
 * This is the FALLBACK implementation of [ReasoningModel].
 * It uses keyword matching to classify observable reasoning patterns.
 *
 * It is always available offline and never fails.
 * It should NOT be presented as "AI" to the user — it is a deterministic fallback.
 *
 * When a LocalReasoningModel (P1) is available, it takes priority.
 * This class remains as the error-safe fallback.
 */
class RuleBasedReasoningModel : ReasoningModel {

    private val linearKeywords = setOf(
        "proportional", "proportionally", "linear", "linearly",
        "directly", "direct", "same rate", "1:1", "one-to-one",
        "doubles when", "increases by same", "increases by the same",
        "equal proportion", "same amount", "uniformly", "straight",
        "constant rate", "velocity and range"
    )

    private val quadraticKeywords = setOf(
        "squared", "square", "quadratic", "v²", "v^2",
        "velocity squared", "exponential", "four times",
        "4 times", "power of 2", "second power", "non-linear",
        "parabola", "parabolic"
    )

    override suspend fun classifyReasoning(explanation: String): ReasoningClassification {
        val lower = explanation.trim().lowercase()

        if (lower.isBlank()) {
            return ReasoningClassification(
                dominantPattern = ReasoningPattern.UNKNOWN,
                confidence = 0.0f,
                candidatePatterns = emptyList(),
                classifiedBy = CLASSIFIER_ID
            )
        }

        val linearScore = linearKeywords.count { lower.contains(it) }
        val quadraticScore = quadraticKeywords.count { lower.contains(it) }

        return when {
            quadraticScore > linearScore -> ReasoningClassification(
                dominantPattern = ReasoningPattern.QUADRATIC_RELATIONSHIP,
                confidence = 0.82f,
                candidatePatterns = listOf(ReasoningPattern.QUADRATIC_RELATIONSHIP),
                classifiedBy = CLASSIFIER_ID
            )
            linearScore > 0 -> ReasoningClassification(
                dominantPattern = ReasoningPattern.LINEAR_PROPORTIONAL,
                confidence = 0.78f,
                candidatePatterns = listOf(
                    ReasoningPattern.LINEAR_PROPORTIONAL,
                    ReasoningPattern.GENERIC_INCREASE
                ),
                classifiedBy = CLASSIFIER_ID
            )
            lower.length > 5 -> {
                // Non-empty explanation with no recognised keywords → assume generic/linear
                ReasoningClassification(
                    dominantPattern = ReasoningPattern.LINEAR_PROPORTIONAL,
                    confidence = 0.52f,
                    candidatePatterns = listOf(
                        ReasoningPattern.LINEAR_PROPORTIONAL,
                        ReasoningPattern.GENERIC_INCREASE
                    ),
                    classifiedBy = CLASSIFIER_ID
                )
            }
            else -> ReasoningClassification(
                dominantPattern = ReasoningPattern.UNKNOWN,
                confidence = 0.0f,
                candidatePatterns = emptyList(),
                classifiedBy = CLASSIFIER_ID
            )
        }
    }

    companion object {
        const val CLASSIFIER_ID = "rule_based_v1"
    }
}
