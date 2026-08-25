# CAUSELY — Implementation Plan
## "AI that experimentally tests how you reason."
### iQOO Hackathon 2026 | Track: Smart Education

---

## Environment Confirmed

| Tool | Version |
|------|---------|
| Java | 26.0.1 |
| Android SDK | `C:\Users\NEELS\AppData\Local\Android\Sdk` |
| Platform | android-36.1 |
| Build Tools | 36.1.0, 37.0.0 |
| ADB | Present in platform-tools |

> [!IMPORTANT]
> Java 26 is higher than the typical Kotlin/AGP requirements (Java 17/21). We will configure `toolchains` in Gradle to compile with Java 17 compatibility to avoid any Kotlin/AGP compatibility issues, while using the installed JDK 26 as the host.

---

## Architecture Overview

```
causely/
├── app/                          # Main application module
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   └── kotlin/com/causely/
│       │       ├── CauselyApp.kt             # Application class
│       │       ├── MainActivity.kt           # Single activity host
│       │       ├── navigation/               # NavGraph
│       │       ├── ui/
│       │       │   ├── theme/                # Material3 dark theme tokens
│       │       │   ├── screens/              # 10 screens
│       │       │   │   ├── HomeScreen
│       │       │   │   ├── PredictionScreen
│       │       │   │   ├── ExplainScreen
│       │       │   │   ├── HypothesesScreen
│       │       │   │   ├── ExperimentScreen
│       │       │   │   ├── LaboratoryScreen
│       │       │   │   ├── EvidenceScreen
│       │       │   │   ├── TransferScreen
│       │       │   │   └── ResultScreen
│       │       │   └── components/           # Reusable UI components
│       │       └── di/                       # Manual DI / Hilt
│       └── test/                             # Unit tests
│
├── core/
│   └── physics/                  # ProjectilePhysicsEngine (pure Kotlin, no Android)
│
├── domain/
│   ├── hypothesis/               # HypothesisEngine + Hypothesis models
│   ├── experiment/               # ExperimentSelector
│   ├── evidence/                 # EvidenceUpdater
│   ├── transfer/                 # TransferEvaluator
│   └── reasoning/                # ReasoningModel interface + rule-based impl
│
└── data/
    └── session/                  # DataStore session persistence
```

### Key Architecture Pattern: MVVM + Clean Architecture

```
Screen → ViewModel → UseCase → Domain Engine → Result → StateFlow → Screen
```

---

## Module Breakdown

### 1. `core/physics` — ProjectilePhysicsEngine

Pure Kotlin, zero Android dependencies. Fully unit-testable.

```kotlin
data class ProjectileState(
    val velocity: Double,      // m/s
    val angleDeg: Double,      // degrees
    val gravity: Double,       // m/s²
    val range: Double,         // metres
    val maxHeight: Double,
    val timeOfFlight: Double,
    val trajectoryPoints: List<Pair<Double,Double>>
)

object ProjectilePhysicsEngine {
    fun calculate(velocity: Double, angleDeg: Double, gravity: Double): ProjectileState
    fun trajectoryPoints(velocity: Double, angleDeg: Double, gravity: Double, steps: Int): List<Pair<Double,Double>>
}
```

**Physics equations:**
- `vx = v * cos(θ)`, `vy = v * sin(θ)`
- `range R = v² * sin(2θ) / g`
- `maxHeight H = vy² / (2g)`
- `timeOfFlight T = 2vy / g`
- `x(t) = vx*t`, `y(t) = vy*t - 0.5*g*t²`

---

### 2. `domain/hypothesis` — HypothesisEngine

```kotlin
data class Hypothesis(
    val id: String,
    val name: String,
    val description: String,
    val supportScore: Double,        // 0.0 to 1.0
    val predictionFunction: (baseRange: Double, velocityRatio: Double) -> Double
)

object HypothesisEngine {
    val linearHypothesis: Hypothesis       // R ∝ v
    val quadraticHypothesis: Hypothesis    // R ∝ v²
    
    fun predictRange(hypothesis: Hypothesis, baseRange: Double, velocityRatio: Double): Double
    fun updateEvidence(hypothesis: Hypothesis, predicted: Double, observed: Double): Hypothesis
}
```

---

### 3. `domain/experiment` — ExperimentSelector

```kotlin
data class CandidateExperiment(
    val id: String,
    val baseVelocity: Double,
    val targetVelocity: Double,
    val angle: Double,
    val gravity: Double
)

data class ExperimentResult(
    val experiment: CandidateExperiment,
    val predictions: Map<String, Double>,  // hypothesisId → predicted range ratio
    val separationScore: Double,
    val selectionReason: String
)

object ExperimentSelector {
    fun selectBest(hypotheses: List<Hypothesis>, candidates: List<CandidateExperiment>): ExperimentResult
    fun scoreSeparation(predictions: Map<String, Double>): Double
}
```

**Scoring algorithm:**
```
separationScore = |pred_H1 - pred_H2| / max(pred_H1, pred_H2)
```
Higher score = better distinguishing power.

---

### 4. `domain/evidence` — EvidenceUpdater

```kotlin
data class EvidenceState(
    val hypotheses: List<Hypothesis>,
    val observedRatio: Double,
    val verdict: EvidenceVerdict
)

enum class EvidenceVerdict { SUPPORTS_LINEAR, SUPPORTS_QUADRATIC, INCONCLUSIVE }

object EvidenceUpdater {
    fun update(
        hypotheses: List<Hypothesis>,
        experiment: CandidateExperiment,
        observedRange: Double,
        baseRange: Double
    ): EvidenceState
}
```

---

### 5. `domain/transfer` — TransferEvaluator

```kotlin
data class TransferProblem(
    val question: String,
    val baseVelocity: Double,
    val targetVelocity: Double,
    val correctAnswerKey: String,     // "4x"
    val options: List<String>
)

data class TransferResult(
    val correct: Boolean,
    val studentAnswer: String,
    val correctAnswer: String,
    val confirmed: Boolean            // CAUSAL TRANSFER CONFIRMED
)

object TransferEvaluator {
    fun evaluate(problem: TransferProblem, studentAnswer: String): TransferResult
    fun generateProblem(fromVelocity: Double, toVelocity: Double): TransferProblem
}
```

---

### 6. `domain/reasoning` — ReasoningModel (AI layer)

```kotlin
interface ReasoningModel {
    suspend fun classifyReasoning(explanation: String): ReasoningClassification
}

data class ReasoningClassification(
    val dominantPattern: ReasoningPattern,
    val confidence: Float,
    val candidatePatterns: List<ReasoningPattern>
)

enum class ReasoningPattern {
    LINEAR_PROPORTIONAL,     // "velocity and range increase proportionally"
    QUADRATIC_RELATIONSHIP,  // v² relationship
    UNKNOWN
}

// Rule-based fallback (always works offline)
class RuleBasedReasoningModel : ReasoningModel

// Optional: future local LLM bridge
// class LocalLLMReasoningModel : ReasoningModel
```

**Rule-based keywords:**
- LINEAR: "proportionally", "linear", "directly", "same rate", "1:1", "doubles"
- QUADRATIC: "squared", "quadratic", "v²", "exponential", "four times"

---

### 7. Session State (DataStore)

```kotlin
// Tracks the full learning session for one concept attempt
data class LearningSession(
    val conceptId: String,
    val predictionAnswer: String,
    val explanation: String,
    val selectedExperiment: CandidateExperiment?,
    val observedRange: Double?,
    val evidenceState: EvidenceState?,
    val transferResult: TransferResult?,
    val completedAt: Long?
)
```

---

## Screen Design

### Screen 1: Home
- Dark background, "CAUSELY" wordmark
- Tagline: "AI that experimentally tests how you reason."
- Card: "Projectile Motion" → taps into Prediction
- "Demo Mode" accessible via subtle button

### Screen 2: Prediction
- Question card: "If we double the launch velocity while keeping everything else constant, what happens to the range?"
- 4 option buttons: 2×, 4×, No change, Other
- Physics diagram animation

### Screen 3: Explain
- "Why do you think so?"
- Text input field
- Voice input button (mic icon)
- Real-time character counter
- "Analyze My Reasoning" CTA

### Screen 4: Hypotheses
- Two hypothesis cards side by side (or stacked)
- H1: Linear — `R ∝ v` with icon
- H2: Quadratic — `R ∝ v²` with icon
- Animated reveal
- "Let's test them →" button

### Screen 5: Experiment Selection
- "We need an experiment that separates these models"
- Shows candidate experiments with separation scores
- Highlights winning experiment
- Shows what H1 predicts vs H2 predicts
- "Enter the Laboratory →" button

### Screen 6: Laboratory (Interactive Simulation)
- **Most important screen**
- Canvas-based projectile animation
- Sliders: velocity, angle, gravity
- Real-time trajectory drawn on canvas
- Range indicator at bottom
- "Run Experiment" shows both velocities side by side
- Numerical results displayed prominently

### Screen 7: Evidence Update
- Before/After comparison
- H1 bar: confidence dropping (animated)
- H2 bar: confidence rising (animated)
- "Observed result = 4× range"
- Explanation text from deterministic engine
- "Continue →" button

### Screen 8: Transfer Test
- New problem card
- "30 m/s → 60 m/s — what happens to range?"
- 4 options
- Timer optional

### Screen 9: Result
- **CAUSAL TRANSFER CONFIRMED ✓** (or retry)
- Learning summary card:
  - Initial Model → Updated Model
  - Evidence → Passed Transfer
- Confetti animation on success

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.x |
| UI | Jetpack Compose + Material3 |
| Architecture | MVVM + Clean Architecture |
| Navigation | Navigation Compose |
| State | StateFlow + ViewModel |
| Local Storage | DataStore Preferences |
| DI | Manual DI (no Hilt for speed) |
| Physics | Pure Kotlin (no library) |
| Animation | Compose Canvas + Animatable |
| Voice | Android SpeechRecognizer |
| Camera | CameraX (P1) |
| Testing | JUnit 4 + Truth |
| Build | AGP 8.6 + Kotlin 2.0 |

---

## Gradle Config

```
compileSdk = 36
minSdk = 26
targetSdk = 36
Java compatibility = 17 (source/target)
Kotlin JVM target = 17
```

---

## Implementation Sequence (30-hour plan)

### Phase 1 — Foundation (Hours 1-4)
- [x] Scaffold Android project structure
- [x] Configure Gradle (Kotlin DSL)
- [x] Set up Material3 dark theme
- [x] Navigation graph skeleton

### Phase 2 — Core Engines (Hours 4-8)
- [x] ProjectilePhysicsEngine + tests
- [x] HypothesisEngine + tests
- [x] ExperimentSelector + tests
- [x] EvidenceUpdater + tests
- [x] TransferEvaluator + tests
- [x] RuleBasedReasoningModel + tests

### Phase 3 — ViewModels (Hours 8-12)
- [x] LearningSessionViewModel (master state machine)
- [x] SimulationViewModel (laboratory physics state)

### Phase 4 — Screens (Hours 12-22)
- [x] HomeScreen
- [x] PredictionScreen
- [x] ExplainScreen
- [x] HypothesesScreen
- [x] ExperimentScreen
- [x] LaboratoryScreen (Canvas simulation)
- [x] EvidenceScreen
- [x] TransferScreen
- [x] ResultScreen

### Phase 5 — Polish (Hours 22-26)
- [x] Animations and transitions
- [x] Voice input integration
- [x] Demo Mode

### Phase 6 — Build & Test (Hours 26-30)
- [x] Run all tests
- [x] Build debug APK
- [x] Manual flow verification

---

## Testing Strategy

| Component | Test Type | What is tested |
|-----------|-----------|---------------|
| ProjectilePhysicsEngine | Unit | Range, height, trajectory accuracy |
| HypothesisEngine | Unit | Prediction functions, evidence update |
| ExperimentSelector | Unit | Separation scoring, best candidate selection |
| EvidenceUpdater | Unit | Support score calculation |
| TransferEvaluator | Unit | Correct answer evaluation |
| RuleBasedReasoningModel | Unit | Keyword classification |
| End-to-end flow | Integration | Full prediction→transfer pipeline |

---

## Open Questions

> [!NOTE]
> These are pre-resolved design decisions — no blocking questions.

1. **Gradle Wrapper version** — Will use 8.10 (stable, works with AGP 8.6)
2. **Compose BOM** — 2024.09.xx (stable with Material3)
3. **No Hilt** — Manual DI for hackathon speed; inject at Application level
4. **Voice** — Platform SpeechRecognizer, graceful degradation if unavailable
5. **Local AI** — Rule-based classifier only for MVP; interface ready for LLM plug-in

---

## Verification Plan

### Automated
```bash
./gradlew test           # All unit tests
./gradlew assembleDebug  # APK build
```

### Manual Demo Flow
1. Launch app → Home screen loads
2. Tap "Projectile Motion" → Prediction screen
3. Select "2×" → Explain screen
4. Type explanation → Hypotheses screen
5. View H1/H2 → Experiment screen
6. View selected experiment → Laboratory
7. Manipulate sliders → see trajectory animate
8. Confirm 4× → Evidence screen
9. View model update → Transfer screen
10. Select "4×" → Result: CAUSAL TRANSFER CONFIRMED ✓
