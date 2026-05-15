package com.neuromesh.crisis.domain.usecase

import com.neuromesh.crisis.data.model.AgentType
import com.neuromesh.crisis.data.model.CrisisType
import com.neuromesh.crisis.data.model.Observation
import com.neuromesh.crisis.data.model.ReasoningStep
import com.neuromesh.crisis.data.model.ReasoningTrace
import com.neuromesh.crisis.data.model.SeverityLevel
import com.neuromesh.crisis.data.model.SituationAssessment
import com.neuromesh.crisis.data.repository.ObservationRepository
import com.neuromesh.crisis.domain.agent.HeuristicDetector
import com.neuromesh.crisis.domain.agent.ObserverAgent
import com.neuromesh.crisis.domain.agent.ReasonerAgent
import com.neuromesh.crisis.infrastructure.ml.Gemma4ModelRunner
import com.neuromesh.crisis.util.Constants
import com.neuromesh.crisis.util.DeviceCapability
import com.neuromesh.crisis.util.Logger
import com.neuromesh.crisis.util.Result
import javax.inject.Inject

/**
 * One detection cycle.
 *
 * Order matters:
 *  1. Read the raw sensor snapshot (cheap).
 *  2. GATE: if no sensor crosses a real threshold, return [Result.Error] with
 *     a benign message and DO NOT touch the model. This is what kills the
 *     random-alerts bug and the constant-inference overheating.
 *  3. Only past the gate do we spend compute:
 *      - capable phone with the model ready -> full Observer+Reasoner LLM path
 *      - low-RAM / no model -> heuristic classifier (still feeds the mesh)
 */
class DetectCrisisUseCase @Inject constructor(
    private val observerAgent: ObserverAgent,
    private val reasonerAgent: ReasonerAgent,
    private val heuristicDetector: HeuristicDetector,
    private val observationRepository: ObservationRepository,
    private val modelRunner: Gemma4ModelRunner,
    private val deviceCapability: DeviceCapability,
    private val deviceId: String
) {

    suspend operator fun invoke(): Result<SituationAssessment> {
        val start = System.currentTimeMillis()

        val snapshot = heuristicDetector.snapshot()
        if (!snapshot.crossesThreshold) {
            return Result.Error(NO_SIGNAL)
        }
        Logger.i(TAG, "Sensor gate passed: ${snapshot.triggerReasons.joinToString("; ")}")

        val useLlm = modelRunner.isReady() && deviceCapability.canHostLlm()

        val localObservation: Observation = if (useLlm) {
            when (val observationResult = observerAgent.analyze()) {
                is Result.Success -> observationResult.data
                is Result.Error -> {
                    Logger.w(TAG, "Observer LLM failed, falling back to heuristic: ${observationResult.message}")
                    heuristicDetector.classify(snapshot)
                        ?: return Result.Error(NO_SIGNAL)
                }
            }
        } else {
            heuristicDetector.classify(snapshot)
                ?: return Result.Error(NO_SIGNAL)
        }

        observationRepository.save(localObservation)

        if (localObservation.confidence < Constants.MIN_OBSERVATION_CONFIDENCE ||
            localObservation.crisisType == CrisisType.UNKNOWN
        ) {
            Logger.d(TAG, "Observation rejected: type=${localObservation.crisisType} conf=${localObservation.confidence}")
            return Result.Error(NO_SIGNAL)
        }

        val recentObs = observationRepository.getRecentSince(
            since = System.currentTimeMillis() - Constants.OBSERVATION_WINDOW_MS
        ).filter { it.crisisType != CrisisType.UNKNOWN }

        val observations = if (recentObs.isEmpty()) listOf(localObservation) else recentObs

        val assessmentResult: Result<SituationAssessment> = if (useLlm) {
            reasonerAgent.reason(observations)
        } else {
            Result.Success(buildHeuristicAssessment(localObservation, observations.map { it.id }))
        }

        val latencyMs = System.currentTimeMillis() - start
        Logger.d(TAG, "Crisis detection pipeline completed in ${latencyMs}ms (llm=$useLlm)")
        if (latencyMs > Constants.MAX_DETECTION_LATENCY_MS) {
            Logger.w(TAG, "Detection latency exceeded target: ${latencyMs}ms")
        }

        return assessmentResult
    }

    private fun buildHeuristicAssessment(
        obs: Observation,
        observationIds: List<String>
    ): SituationAssessment {
        val severity = when {
            obs.confidence >= 0.85f -> SeverityLevel.HIGH
            obs.confidence >= 0.7f -> SeverityLevel.MODERATE
            else -> SeverityLevel.LOW
        }
        val trace = ReasoningTrace(
            agentId = deviceId,
            agentType = AgentType.REASONER,
            timestamp = System.currentTimeMillis(),
            inputs = observationIds,
            reasoning = obs.rawText,
            conclusion = "Rule-based assessment: ${obs.crisisType} (no on-device LLM on this device).",
            confidence = obs.confidence,
            steps = listOf(
                ReasoningStep(
                    stepNumber = 1,
                    description = "Sensor thresholds crossed for ${obs.crisisType}",
                    evidence = obs.rawText,
                    confidence = obs.confidence
                )
            )
        )
        return SituationAssessment(
            id = "${System.currentTimeMillis()}_${(1000..9999).random()}",
            deviceId = deviceId,
            timestamp = System.currentTimeMillis(),
            crisisType = obs.crisisType,
            severity = severity,
            confidence = obs.confidence,
            affectedArea = "Local area near reporting device",
            estimatedAffected = 0,
            immediateRisks = riskHints(obs.crisisType),
            observations = observationIds,
            reasoningTrace = trace
        )
    }

    private fun riskHints(type: CrisisType): List<String> = when (type) {
        CrisisType.FIRE -> listOf("Smoke inhalation", "Fire spread", "Blocked exits")
        CrisisType.EARTHQUAKE -> listOf("Falling debris", "Structural collapse", "Aftershocks")
        CrisisType.FLOOD -> listOf("Rising water", "Electrocution", "Loss of footing")
        CrisisType.STRUCTURAL_COLLAPSE -> listOf("Falling debris", "Trapped persons")
        CrisisType.MEDICAL_EMERGENCY -> listOf("Loss of consciousness")
        CrisisType.UNKNOWN -> emptyList()
    }

    companion object {
        private const val TAG = "DetectCrisisUseCase"
        const val NO_SIGNAL = "NO_SIGNAL"
    }
}
