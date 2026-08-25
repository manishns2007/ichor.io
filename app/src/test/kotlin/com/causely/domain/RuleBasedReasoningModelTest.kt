package com.causely.domain

import com.causely.domain.reasoning.ReasoningPattern
import com.causely.domain.reasoning.RuleBasedReasoningModel
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RuleBasedReasoningModelTest {

    private val model = RuleBasedReasoningModel()

    @Test
    fun `proportionally keyword maps to linear pattern`() = runTest {
        val result = model.classifyReasoning("Because velocity and range increase proportionally.")
        assertThat(result.dominantPattern).isEqualTo(ReasoningPattern.LINEAR_PROPORTIONAL)
    }

    @Test
    fun `linear keyword maps to linear pattern`() = runTest {
        val result = model.classifyReasoning("The relationship is linear.")
        assertThat(result.dominantPattern).isEqualTo(ReasoningPattern.LINEAR_PROPORTIONAL)
    }

    @Test
    fun `squared keyword maps to quadratic pattern`() = runTest {
        val result = model.classifyReasoning("Range increases with velocity squared.")
        assertThat(result.dominantPattern).isEqualTo(ReasoningPattern.QUADRATIC_RELATIONSHIP)
    }

    @Test
    fun `v squared keyword maps to quadratic pattern`() = runTest {
        val result = model.classifyReasoning("Because R is proportional to v².")
        assertThat(result.dominantPattern).isEqualTo(ReasoningPattern.QUADRATIC_RELATIONSHIP)
    }

    @Test
    fun `empty explanation maps to unknown`() = runTest {
        val result = model.classifyReasoning("")
        assertThat(result.dominantPattern).isEqualTo(ReasoningPattern.UNKNOWN)
        assertThat(result.confidence).isEqualTo(0.0f)
    }

    @Test
    fun `blank explanation maps to unknown`() = runTest {
        val result = model.classifyReasoning("   ")
        assertThat(result.dominantPattern).isEqualTo(ReasoningPattern.UNKNOWN)
    }

    @Test
    fun `classified by is rule_based_v1`() = runTest {
        val result = model.classifyReasoning("Because velocity and range are proportional.")
        assertThat(result.classifiedBy).isEqualTo("rule_based_v1")
    }

    @Test
    fun `confidence is greater than zero for non-empty explanation`() = runTest {
        val result = model.classifyReasoning("I think the range goes up when velocity increases.")
        assertThat(result.confidence).isGreaterThan(0.0f)
    }

    @Test
    fun `quadratic keywords beat linear keywords`() = runTest {
        // Text mentions "squared" (quadratic keyword) once and no linear keywords
        val result = model.classifyReasoning("The range increases by squared velocity.")
        assertThat(result.dominantPattern).isEqualTo(ReasoningPattern.QUADRATIC_RELATIONSHIP)
    }
}
