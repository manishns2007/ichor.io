package com.causely.domain

import com.causely.domain.transfer.TransferEvaluator
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TransferEvaluatorTest {

    @Test
    fun `problem for 30 to 60 ms has correct option labelled 4x`() {
        val problem = TransferEvaluator.generateProblem(30.0, 60.0)
        val correctOption = problem.options.find { it.id == "correct" }!!
        assertThat(correctOption.label).isEqualTo("4×")
    }

    @Test
    fun `correct answer id is always correct`() {
        val problem = TransferEvaluator.generateProblem(30.0, 60.0)
        assertThat(problem.correctOptionId).isEqualTo("correct")
    }

    @Test
    fun `selecting correct option confirms transfer`() {
        val problem = TransferEvaluator.generateProblem(30.0, 60.0)
        val result = TransferEvaluator.evaluate(problem, "correct")
        assertThat(result.correct).isTrue()
        assertThat(result.transferConfirmed).isTrue()
    }

    @Test
    fun `selecting wrong option does not confirm transfer`() {
        val problem = TransferEvaluator.generateProblem(30.0, 60.0)
        val result = TransferEvaluator.evaluate(problem, "linear")
        assertThat(result.correct).isFalse()
        assertThat(result.transferConfirmed).isFalse()
    }

    @Test
    fun `feedback for correct answer contains CAUSAL TRANSFER CONFIRMED`() {
        val problem = TransferEvaluator.generateProblem(30.0, 60.0)
        val result = TransferEvaluator.evaluate(problem, "correct")
        assertThat(result.feedback).contains("CAUSAL TRANSFER CONFIRMED")
    }

    @Test
    fun `options list contains at least 3 entries`() {
        val problem = TransferEvaluator.generateProblem(30.0, 60.0)
        assertThat(problem.options.size).isAtLeast(3)
    }

    @Test
    fun `options list always contains the correct option`() {
        val problem = TransferEvaluator.generateProblem(30.0, 60.0)
        assertThat(problem.options.map { it.id }).contains("correct")
    }

    @Test
    fun `question contains velocity values`() {
        val problem = TransferEvaluator.generateProblem(30.0, 60.0)
        assertThat(problem.question).contains("30")
        assertThat(problem.question).contains("60")
    }

    @Test
    fun `formatRatio returns 4x for value 4`() {
        assertThat(TransferEvaluator.formatRatio(4.0)).isEqualTo("4×")
    }

    @Test
    fun `formatRatio returns 9x for value 9`() {
        assertThat(TransferEvaluator.formatRatio(9.0)).isEqualTo("9×")
    }

    @Test
    fun `formatRatio returns no change for value 1`() {
        assertThat(TransferEvaluator.formatRatio(1.0)).contains("1×")
    }

    @Test
    fun `problem for 10 to 30 ms has correct option labelled 9x`() {
        // vRatio = 3, rangeRatio = 9
        val problem = TransferEvaluator.generateProblem(10.0, 30.0)
        val correctOption = problem.options.find { it.id == "correct" }!!
        assertThat(correctOption.label).isEqualTo("9×")
    }
}
