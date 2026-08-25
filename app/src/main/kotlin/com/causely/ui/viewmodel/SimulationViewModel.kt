package com.causely.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.causely.core.physics.ProjectilePhysicsEngine
import com.causely.core.physics.ProjectileResult
import com.causely.domain.experiment.CandidateExperiment
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SimulationState(
    val velocity: Float = 20f,        // m/s — slider-controlled
    val angleDeg: Float = 45f,        // degrees — slider-controlled
    val gravity: Float = 9.8f,        // m/s² — slider-controlled
    val currentResult: ProjectileResult? = null,
    val comparisonResult: ProjectileResult? = null,
    val showComparison: Boolean = false,
    val animationProgress: Float = 0f, // 0f=idle, 0f..1f=animating, 1f=complete
    val isAnimating: Boolean = false,
    val experimentRan: Boolean = false  // true after both conditions have been shown
)

/**
 * ViewModel for the Laboratory screen.
 *
 * Manages the interactive projectile simulation state.
 * All physics calculations are delegated to [ProjectilePhysicsEngine].
 */
class SimulationViewModel : ViewModel() {

    private val _state = MutableStateFlow(SimulationState())
    val state: StateFlow<SimulationState> = _state.asStateFlow()

    /** Recalculate the current trajectory from slider values. */
    private fun recalculate() {
        val s = _state.value
        if (s.velocity <= 0f || s.angleDeg !in 0f..90f || s.gravity <= 0f) return
        val result = ProjectilePhysicsEngine.calculate(
            velocity = s.velocity.toDouble(),
            angleDeg = s.angleDeg.toDouble(),
            gravity = s.gravity.toDouble()
        )
        _state.update { it.copy(currentResult = result) }
    }

    fun setVelocity(v: Float) {
        _state.update { it.copy(velocity = v.coerceIn(1f, 100f)) }
        recalculate()
    }

    fun setAngle(a: Float) {
        _state.update { it.copy(angleDeg = a.coerceIn(1f, 89f)) }
        recalculate()
    }

    fun setGravity(g: Float) {
        _state.update { it.copy(gravity = g.coerceIn(0.5f, 25f)) }
        recalculate()
    }

    /** Initialise the lab with the selected experiment's parameters. */
    fun loadExperiment(experiment: CandidateExperiment) {
        _state.update {
            SimulationState(
                velocity = experiment.baseVelocity.toFloat(),
                angleDeg = experiment.angleDeg.toFloat(),
                gravity = experiment.gravity.toFloat()
            )
        }
        recalculate()
    }

    /**
     * Run the experiment: compute base and target trajectories,
     * then animate the projectile ball along both.
     */
    fun runExperiment(experiment: CandidateExperiment) {
        if (_state.value.isAnimating) return

        val baseResult = ProjectilePhysicsEngine.calculate(
            experiment.baseVelocity, experiment.angleDeg, experiment.gravity
        )
        val expResult = ProjectilePhysicsEngine.calculate(
            experiment.targetVelocity, experiment.angleDeg, experiment.gravity
        )

        _state.update {
            it.copy(
                currentResult = baseResult,
                comparisonResult = expResult,
                showComparison = true,
                isAnimating = true,
                animationProgress = 0f,
                experimentRan = true
            )
        }

        // Drive animation progress from 0→1 over 2.5 seconds
        viewModelScope.launch {
            val steps = 60
            repeat(steps + 1) { i ->
                _state.update { it.copy(animationProgress = i.toFloat() / steps) }
                delay(2500L / steps)
            }
            _state.update { it.copy(isAnimating = false, animationProgress = 1f) }
        }
    }

    fun resetAnimation() {
        _state.update { it.copy(animationProgress = 0f, isAnimating = false) }
    }
}
