package com.causely.navigation

/**
 * All navigation destinations in Causely.
 * Navigation is linear: each screen advances the learning loop.
 */
sealed class CauselyRoute(val route: String) {
    object Home : CauselyRoute("home")
    object Prediction : CauselyRoute("prediction")
    object Explain : CauselyRoute("explain")
    object Hypotheses : CauselyRoute("hypotheses")
    object Experiment : CauselyRoute("experiment")
    object Laboratory : CauselyRoute("laboratory")
    object Evidence : CauselyRoute("evidence")
    object Transfer : CauselyRoute("transfer")
    object Result : CauselyRoute("result")
}
