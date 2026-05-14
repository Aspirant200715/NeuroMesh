package com.neuromesh.crisis.domain.usecase

import com.neuromesh.crisis.data.model.CrisisType
import com.neuromesh.crisis.data.model.Observation
import com.neuromesh.crisis.data.model.SituationAssessment
import com.neuromesh.crisis.data.repository.ObservationRepository
import com.neuromesh.crisis.domain.agent.ActionAgent
import com.neuromesh.crisis.domain.agent.ObserverAgent
import com.neuromesh.crisis.domain.agent.ReasonerAgent
import com.neuromesh.crisis.util.Constants
import com.neuromesh.crisis.util.Logger
import com.neuromesh.crisis.util.Result
import javax.inject.Inject

class DetectCrisisUseCase @Inject constructor(
    private val observerAgent: ObserverAgent,
    private val reasonerAgent: ReasonerAgent,
    private val observationRepository: ObservationRepository
) {

    suspend operator fun invoke(): Result<SituationAssessment> {
        val start = System.currentTimeMillis()

        val observationResult = observerAgent.analyze()
        if (observationResult is Result.Error) {
            return Result.Error("Observer failed: ${observationResult.message}")
        }
        val localObservation = (observationResult as Result.Success).data

        observationRepository.save(localObservation)

        if (localObservation.confidence < Constants.MIN_OBSERVATION_CONFIDENCE) {
            Logger.d(TAG, "Observation confidence too low: ${localObservation.confidence}")
            return Result.Error("Confidence below threshold: ${localObservation.confidence}")
        }

        val recentObs = observationRepository.getRecentSince(
            since = System.currentTimeMillis() - Constants.OBSERVATION_WINDOW_MS
        ).filter { it.crisisType != CrisisType.UNKNOWN }

        val observations = if (recentObs.isEmpty()) listOf(localObservation) else recentObs

        val assessmentResult = reasonerAgent.reason(observations)

        val latencyMs = System.currentTimeMillis() - start
        Logger.d(TAG, "Crisis detection pipeline completed in ${latencyMs}ms")

        if (latencyMs > Constants.MAX_DETECTION_LATENCY_MS) {
            Logger.w(TAG, "Detection latency exceeded target: ${latencyMs}ms > ${Constants.MAX_DETECTION_LATENCY_MS}ms")
        }

        return assessmentResult
    }

    companion object {
        private const val TAG = "DetectCrisisUseCase"
    }
}
