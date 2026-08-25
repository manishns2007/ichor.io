package com.causely.domain.transfer

import kotlin.math.abs

/**
 * A single answer option for the transfer problem.
 */
data class TransferOption(
    val id: String,
    val label: String
)

/**
 * A conceptual transfer problem to test whether the student can
 * apply the updated model (R ∝ v²) to a new scenario.
 *
 * The problem uses different velocity values than the original experiment
 * to test genuine transfer rather than pattern matching.
 */
data class TransferProblem(
    val question: String,
    val baseVelocity: Double,
    val targetVelocity: Double,
    val options: List<TransferOption>,
    val correctOptionId: String
)

/**
 * Result of evaluating the student's answer to the transfer problem.
 *
 * @param correct           whether the student's answer is correct
 * @param studentAnswerId   the option id chosen by the student
 * @param correctOptionId   the correct option id
 * @param correctLabel      human-readable label of the correct answer
 * @param transferConfirmed true when the student correctly applies the updated model
 * @param feedback          feedback message to display
 */
data class TransferResult(
    val correct: Boolean,
    val studentAnswerId: String,
    val correctOptionId: String,
    val correctLabel: String,
    val transferConfirmed: Boolean,
    val feedback: String
)

/**
 * Generates and evaluates conceptual transfer problems.
 *
 * The correct answer is always computed from R ∝ v² (the quadratic model):
 *   range ratio = (v₂/v₁)²
 *
 * The physics engine does NOT need to be called here — the formula is deterministic.
 */
object TransferEvaluator {

    /**
     * Generate a transfer problem for the given velocity change.
     * Uses velocity values different from the original experiment (30→60 m/s for the MVP).
     */
    fun generateProblem(fromVelocity: Double, toVelocity: Double): TransferProblem {
        val velocityRatio = toVelocity / fromVelocity
        val correctRangeRatio = velocityRatio * velocityRatio   // R ∝ v²
        val linearRangeRatio = velocityRatio                    // R ∝ v (misconception)

        val correctLabel = formatRatio(correctRangeRatio)
        val linearLabel = formatRatio(linearRangeRatio)

        val options = buildOptions(correctLabel, linearLabel)

        return TransferProblem(
            question = "A projectile is launched at ${fromVelocity.toInt()} m/s. " +
                    "The velocity is increased to ${toVelocity.toInt()} m/s " +
                    "(same launch angle, same gravity). What happens to the range?",
            baseVelocity = fromVelocity,
            targetVelocity = toVelocity,
            options = options,
            correctOptionId = "correct"
        )
    }

    /**
     * Evaluate the student's answer to a transfer problem.
     *
     * @param problem         the transfer problem
     * @param studentAnswerId the id of the option the student selected
     */
    fun evaluate(problem: TransferProblem, studentAnswerId: String): TransferResult {
        val correct = studentAnswerId == problem.correctOptionId
        val correctOption = problem.options.find { it.id == problem.correctOptionId }!!
        val vRatioStr = formatRatio(problem.targetVelocity / problem.baseVelocity)
        val rRatioStr = correctOption.label

        return TransferResult(
            correct = correct,
            studentAnswerId = studentAnswerId,
            correctOptionId = problem.correctOptionId,
            correctLabel = correctOption.label,
            transferConfirmed = correct,
            feedback = if (correct) {
                "CAUSAL TRANSFER CONFIRMED ✓\n" +
                        "You correctly applied R ∝ v² to a new scenario. " +
                        "A ${vRatioStr} velocity change → ${rRatioStr} range change."
            } else {
                "The quadratic relationship (R ∝ v²) applies here: " +
                        "a ${vRatioStr} velocity change produces a ${rRatioStr} range change."
            }
        )
    }

    fun formatRatio(ratio: Double): String = when {
        abs(ratio - 1.0) < 0.05 -> "No change (1×)"
        abs(ratio - 2.0) < 0.1 -> "2×"
        abs(ratio - 3.0) < 0.1 -> "3×"
        abs(ratio - 4.0) < 0.1 -> "4×"
        abs(ratio - 9.0) < 0.1 -> "9×"
        abs(ratio - 16.0) < 0.1 -> "16×"
        else -> "${"%.1f".format(ratio)}×"
    }

    private fun buildOptions(correctLabel: String, linearLabel: String): List<TransferOption> {
        val opts = mutableListOf(
            TransferOption("correct", correctLabel),       // R ∝ v² (correct)
            TransferOption("no_change", "No change (1×)") // no understanding
        )
        // Add linear option only if it's distinct from the correct answer
        if (linearLabel != correctLabel && linearLabel != "No change (1×)") {
            opts.add(TransferOption("linear", linearLabel))  // R ∝ v (common misconception)
        }
        opts.add(TransferOption("other", "Other"))
        // Deterministic order for demo consistency — correct answer is last in the list
        // so the student must actually think rather than auto-selecting the first option.
        return opts.reversed()
    }
}
