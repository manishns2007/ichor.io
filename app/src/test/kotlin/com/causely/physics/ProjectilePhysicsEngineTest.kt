package com.causely.physics

import com.causely.core.physics.ProjectilePhysicsEngine
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.math.abs
import kotlin.math.sin

class ProjectilePhysicsEngineTest {

    private val epsilon = 0.001  // 0.1% tolerance for floating-point comparison

    // Helper: assert approximately equal
    private fun assertNear(expected: Double, actual: Double, tol: Double = epsilon) {
        assertThat(abs(actual - expected)).isLessThan(tol)
    }

    @Test
    fun `range formula at 45 degrees matches v squared over g`() {
        // R = v²·sin(2θ)/g. At θ=45°, sin(90°)=1, so R = v²/g
        val v = 20.0
        val g = 9.8
        val expected = (v * v) / g
        val result = ProjectilePhysicsEngine.calculate(v, 45.0, g)
        assertNear(expected, result.range, 0.01)
    }

    @Test
    fun `range ratio equals velocity ratio squared at fixed angle and gravity`() {
        // R ∝ v² at fixed θ, g ⟹ R₂/R₁ = (v₂/v₁)²
        val v1 = 20.0
        val v2 = 40.0
        val ratio = ProjectilePhysicsEngine.rangeRatio(v1, v2, 45.0, 9.8)
        // Expected: (40/20)² = 4.0
        assertNear(4.0, ratio, 0.01)
    }

    @Test
    fun `doubling velocity at 45 degrees quadruples range`() {
        val r1 = ProjectilePhysicsEngine.calculate(20.0, 45.0, 9.8).range
        val r2 = ProjectilePhysicsEngine.calculate(40.0, 45.0, 9.8).range
        assertNear(4.0, r2 / r1, 0.01)
    }

    @Test
    fun `tripling velocity at 45 degrees gives 9x range`() {
        val r1 = ProjectilePhysicsEngine.calculate(10.0, 45.0, 9.8).range
        val r3 = ProjectilePhysicsEngine.calculate(30.0, 45.0, 9.8).range
        assertNear(9.0, r3 / r1, 0.01)
    }

    @Test
    fun `max height at 45 degrees is vy squared over 2g`() {
        val v = 20.0
        val g = 9.8
        val vy = v * sin(Math.toRadians(45.0))
        val expected = (vy * vy) / (2.0 * g)
        val result = ProjectilePhysicsEngine.calculate(v, 45.0, g)
        assertNear(expected, result.maxHeight, 0.01)
    }

    @Test
    fun `time of flight at 45 degrees is 2vy over g`() {
        val v = 20.0
        val g = 9.8
        val vy = v * Math.sin(Math.toRadians(45.0))
        val expected = 2.0 * vy / g
        val result = ProjectilePhysicsEngine.calculate(v, 45.0, g)
        assertNear(expected, result.timeOfFlight, 0.001)
    }

    @Test
    fun `zero velocity gives zero range and height`() {
        val result = ProjectilePhysicsEngine.calculate(0.0, 45.0, 9.8)
        assertThat(result.range).isEqualTo(0.0)
        assertThat(result.maxHeight).isEqualTo(0.0)
        assertThat(result.timeOfFlight).isEqualTo(0.0)
    }

    @Test
    fun `trajectory starts at origin`() {
        val result = ProjectilePhysicsEngine.calculate(20.0, 45.0, 9.8)
        val first = result.trajectoryPoints.first()
        assertNear(0.0, first.x, 0.0001)
        assertNear(0.0, first.y, 0.0001)
    }

    @Test
    fun `trajectory ends at ground with range x`() {
        val result = ProjectilePhysicsEngine.calculate(20.0, 45.0, 9.8)
        val last = result.trajectoryPoints.last()
        assertNear(result.range, last.x, 0.1)
        assertThat(last.y).isAtMost(0.001)
    }

    @Test
    fun `all trajectory y values are non-negative`() {
        val result = ProjectilePhysicsEngine.calculate(30.0, 60.0, 9.8)
        result.trajectoryPoints.forEach { pt ->
            assertThat(pt.y).isAtLeast(-0.0001)  // small tolerance for clamping edge
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative velocity throws`() {
        ProjectilePhysicsEngine.calculate(-1.0, 45.0, 9.8)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `angle above 90 throws`() {
        ProjectilePhysicsEngine.calculate(20.0, 95.0, 9.8)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `zero gravity throws`() {
        ProjectilePhysicsEngine.calculate(20.0, 45.0, 0.0)
    }

    @Test
    fun `range on moon is approximately 6x range on earth at same velocity`() {
        // Moon gravity ≈ 1.62 m/s², Earth ≈ 9.8 m/s²
        // R ∝ 1/g at fixed v and θ, so ratio = 9.8/1.62 ≈ 6.05
        val earthRange = ProjectilePhysicsEngine.calculate(20.0, 45.0, 9.8).range
        val moonRange = ProjectilePhysicsEngine.calculate(20.0, 45.0, 1.62).range
        assertNear(9.8 / 1.62, moonRange / earthRange, 0.01)
    }
}
