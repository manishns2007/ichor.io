package com.causely.core.physics

import kotlin.math.cos
import kotlin.math.sin

/**
 * Projectile trajectory sample point.
 *
 * @param x  horizontal displacement (metres)
 * @param y  vertical displacement above launch point (metres, clamped ≥ 0)
 * @param t  elapsed time (seconds)
 */
data class TrajectoryPoint(
    val x: Double,
    val y: Double,
    val t: Double
)

/**
 * Complete result of a projectile motion calculation.
 *
 * Equations used (standard Newtonian projectile motion):
 *   vx = v·cos(θ),  vy = v·sin(θ)
 *   T  = 2·vy / g                  (time of flight)
 *   R  = v²·sin(2θ) / g            (range — valid at fixed θ, g)
 *   H  = vy² / (2g)                (maximum height)
 *   x(t) = vx·t
 *   y(t) = vy·t − ½·g·t²
 *
 * Note: R ∝ v² only when θ and g are held constant.
 */
data class ProjectileResult(
    val initialVelocity: Double,   // m/s
    val angleDeg: Double,          // degrees
    val gravity: Double,           // m/s²
    val range: Double,             // metres
    val maxHeight: Double,         // metres
    val timeOfFlight: Double,      // seconds
    val trajectoryPoints: List<TrajectoryPoint>
)

/**
 * Deterministic projectile physics engine.
 *
 * All calculations use exact analytic equations. No approximations.
 * The AI layer MUST NOT call this engine; only domain and UI layers may.
 */
object ProjectilePhysicsEngine {

    /**
     * Calculate full projectile motion for given parameters.
     *
     * @param velocity  launch speed in m/s  (must be ≥ 0)
     * @param angleDeg  launch angle in degrees [0, 90]
     * @param gravity   gravitational acceleration in m/s² (must be > 0)
     * @param steps     number of evenly-spaced trajectory sample points (default 120)
     * @return          [ProjectileResult] with trajectory, range, height, and time of flight
     */
    fun calculate(
        velocity: Double,
        angleDeg: Double,
        gravity: Double,
        steps: Int = 120
    ): ProjectileResult {
        require(velocity >= 0.0) { "Velocity must be non-negative, got $velocity" }
        require(angleDeg in 0.0..90.0) { "Angle must be in [0, 90] degrees, got $angleDeg" }
        require(gravity > 0.0) { "Gravity must be positive, got $gravity" }
        require(steps > 0) { "Steps must be positive, got $steps" }

        val angleRad = Math.toRadians(angleDeg)
        val vx = velocity * cos(angleRad)
        val vy = velocity * sin(angleRad)

        val timeOfFlight = if (vy > 0.0) 2.0 * vy / gravity else 0.0
        val range = vx * timeOfFlight
        val maxHeight = if (vy > 0.0) (vy * vy) / (2.0 * gravity) else 0.0

        val trajectoryPoints = buildTrajectory(vx, vy, gravity, timeOfFlight, steps)

        return ProjectileResult(
            initialVelocity = velocity,
            angleDeg = angleDeg,
            gravity = gravity,
            range = range,
            maxHeight = maxHeight,
            timeOfFlight = timeOfFlight,
            trajectoryPoints = trajectoryPoints
        )
    }

    /**
     * Compute the range ratio R₂/R₁ for two velocities at the same angle and gravity.
     *
     * Theoretical result at fixed θ and g:
     *   R = v²·sin(2θ)/g  ⟹  R₂/R₁ = (v₂/v₁)²
     *
     * This function uses the physics engine (not the formula directly) so it
     * serves as a cross-validation path.
     */
    fun rangeRatio(
        v1: Double,
        v2: Double,
        angleDeg: Double,
        gravity: Double
    ): Double {
        val r1 = calculate(v1, angleDeg, gravity).range
        val r2 = calculate(v2, angleDeg, gravity).range
        return if (r1 > 0.0) r2 / r1 else 0.0
    }

    private fun buildTrajectory(
        vx: Double,
        vy: Double,
        gravity: Double,
        timeOfFlight: Double,
        steps: Int
    ): List<TrajectoryPoint> {
        if (timeOfFlight <= 0.0) return listOf(TrajectoryPoint(0.0, 0.0, 0.0))
        return (0..steps).map { i ->
            val t = timeOfFlight * i.toDouble() / steps.toDouble()
            val y = vy * t - 0.5 * gravity * t * t
            TrajectoryPoint(
                x = vx * t,
                y = y.coerceAtLeast(0.0), // clamp to ground
                t = t
            )
        }
    }
}
