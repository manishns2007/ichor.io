package com.causely

import android.app.Application

/**
 * Causely Application class.
 *
 * Provides application-level DI (manual — no Hilt required for hackathon).
 * All domain engines are singletons (Kotlin objects) — no initialization needed.
 * The ReasoningModel defaults to RuleBasedReasoningModel (always available offline).
 */
class CauselyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // All core engines (ProjectilePhysicsEngine, HypothesisEngine,
        // ExperimentSelector, EvidenceUpdater, TransferEvaluator) are
        // Kotlin objects and initialize lazily.
        //
        // P1: Initialize LocalReasoningModel here when available.
        // P1: Initialize StubOfficeKitBridge here.
    }
}
