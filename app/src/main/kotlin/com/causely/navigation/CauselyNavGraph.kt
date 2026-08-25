package com.causely.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.causely.ui.screens.*
import com.causely.ui.viewmodel.LearningSessionViewModel
import com.causely.ui.viewmodel.SimulationViewModel

/**
 * Complete navigation graph for Causely.
 *
 * Navigation is driven by user actions and ViewModel state transitions.
 * All state lives in [LearningSessionViewModel] — screens are stateless composables.
 */
@Composable
fun CauselyNavGraph(
    navController: NavHostController,
    sessionViewModel: LearningSessionViewModel = viewModel(),
    simViewModel: SimulationViewModel = viewModel()
) {
    val sessionState by sessionViewModel.state.collectAsState()
    val simState by simViewModel.state.collectAsState()

    // Track the selected transfer answer locally (UI-only state)
    var selectedTransferAnswerId by remember { mutableStateOf<String?>(null) }

    NavHost(
        navController = navController,
        startDestination = CauselyRoute.Home.route
    ) {

        // ─── Home ──────────────────────────────────────────────────────────
        composable(CauselyRoute.Home.route) {
            HomeScreen(
                onStartProjectileMotion = {
                    sessionViewModel.reset()
                    navController.navigate(CauselyRoute.Prediction.route)
                },
                onStartDemoMode = {
                    sessionViewModel.startDemoMode()
                    navController.navigate(CauselyRoute.Prediction.route)
                }
            )
        }

        // ─── Prediction ────────────────────────────────────────────────────
        composable(CauselyRoute.Prediction.route) {
            PredictionScreen(
                selectedAnswer = sessionState.predictionAnswer,
                onSelectAnswer = { sessionViewModel.selectPrediction(it) },
                onContinue = {
                    navController.navigate(CauselyRoute.Explain.route)
                }
            )
        }

        // ─── Explain ───────────────────────────────────────────────────────
        composable(CauselyRoute.Explain.route) {
            // Navigate to Hypotheses when analysis completes
            LaunchedEffect(sessionState.hypotheses) {
                if (sessionState.hypotheses.isNotEmpty() && !sessionState.isAnalyzingReasoning) {
                    navController.navigate(CauselyRoute.Hypotheses.route) {
                        launchSingleTop = true
                    }
                }
            }

            ExplainScreen(
                prediction = sessionState.predictionAnswer,
                explanationText = sessionState.explanationText,
                isAnalyzing = sessionState.isAnalyzingReasoning,
                onExplanationChanged = { sessionViewModel.updateExplanation(it) },
                onAnalyze = { sessionViewModel.analyzeReasoning() },
                onVoiceInput = { /* P1: Voice input — text fallback active */ }
            )
        }

        // ─── Hypotheses ────────────────────────────────────────────────────
        composable(CauselyRoute.Hypotheses.route) {
            HypothesesScreen(
                hypotheses = sessionState.hypotheses,
                studentExplanation = sessionState.explanationText,
                onContinue = {
                    navController.navigate(CauselyRoute.Experiment.route)
                }
            )
        }

        // ─── Experiment ────────────────────────────────────────────────────
        composable(CauselyRoute.Experiment.route) {
            ExperimentScreen(
                selectedExperiment = sessionState.selectedExperiment,
                onEnterLaboratory = {
                    // Pre-load the lab with the experiment parameters
                    sessionState.selectedExperiment?.experiment?.let { exp ->
                        simViewModel.loadExperiment(exp)
                    }
                    navController.navigate(CauselyRoute.Laboratory.route)
                }
            )
        }

        // ─── Laboratory ────────────────────────────────────────────────────
        composable(CauselyRoute.Laboratory.route) {
            // Navigate to Evidence when observation confirmed
            LaunchedEffect(sessionState.evidenceState) {
                if (sessionState.evidenceState != null) {
                    navController.navigate(CauselyRoute.Evidence.route) {
                        launchSingleTop = true
                    }
                }
            }

            LaboratoryScreen(
                selectedExperiment = sessionState.selectedExperiment,
                simState = simState,
                onVelocityChanged = { simViewModel.setVelocity(it) },
                onAngleChanged = { simViewModel.setAngle(it) },
                onGravityChanged = { simViewModel.setGravity(it) },
                onRunExperiment = {
                    sessionState.selectedExperiment?.experiment?.let { exp ->
                        simViewModel.runExperiment(exp)
                        sessionViewModel.runExperiment()
                    }
                },
                onConfirmObservation = {
                    sessionViewModel.confirmObservation()
                }
            )
        }

        // ─── Evidence ──────────────────────────────────────────────────────
        composable(CauselyRoute.Evidence.route) {
            EvidenceScreen(
                evidenceState = sessionState.evidenceState,
                onContinue = {
                    navController.navigate(CauselyRoute.Transfer.route)
                }
            )
        }

        // ─── Transfer ──────────────────────────────────────────────────────
        composable(CauselyRoute.Transfer.route) {
            // Navigate to Result when transfer is evaluated
            LaunchedEffect(sessionState.transferResult) {
                if (sessionState.transferResult != null) {
                    navController.navigate(CauselyRoute.Result.route) {
                        launchSingleTop = true
                    }
                }
            }

            TransferScreen(
                transferProblem = sessionState.transferProblem,
                selectedAnswerId = selectedTransferAnswerId,
                onSelectAnswer = { selectedTransferAnswerId = it },
                onSubmit = {
                    selectedTransferAnswerId?.let { answerId ->
                        sessionViewModel.submitTransferAnswer(answerId)
                    }
                }
            )
        }

        // ─── Result ────────────────────────────────────────────────────────
        composable(CauselyRoute.Result.route) {
            ResultScreen(
                transferResult = sessionState.transferResult,
                onStartAgain = {
                    selectedTransferAnswerId = null
                    sessionViewModel.reset()
                    navController.navigate(CauselyRoute.Home.route) {
                        popUpTo(CauselyRoute.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
