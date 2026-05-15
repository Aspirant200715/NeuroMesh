package com.neuromesh.crisis.domain.consensus

import com.neuromesh.crisis.data.model.*
import com.neuromesh.crisis.util.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsensusEngine @Inject constructor(
    private val votingStrategy: VotingStrategy,
    private val gossipProtocol: GossipProtocol
) {
    private val pendingAssessments = mutableMapOf<String, SituationAssessment>()

    private val _consensusEvents = MutableSharedFlow<ConsensusEvent>(replay = 1)
    val consensusEvents: Flow<ConsensusEvent> = _consensusEvents.asSharedFlow()

    suspend fun submitAssessment(assessment: SituationAssessment) {
        pendingAssessments[assessment.deviceId] = assessment
        Logger.d(TAG, "Assessment added from ${assessment.deviceId.take(8)}, total=${pendingAssessments.size}")

        evaluateConsensus()
    }

    private suspend fun evaluateConsensus() {
        val recentAssessments = pendingAssessments.values
            .filter { System.currentTimeMillis() - it.timestamp < CONSENSUS_WINDOW_MS }
            .toList()

        // Consensus is only meaningful across MULTIPLE distinct devices. With a
        // single device the local detection path already raised its own alert;
        // emitting a "consensus" here too would double-alert and let one phone
        // declare mesh consensus by itself.
        val distinctDevices = recentAssessments.map { it.deviceId }.distinct().size
        if (distinctDevices < VotingStrategy.MIN_DEVICES_FOR_CONSENSUS) {
            _consensusEvents.emit(ConsensusEvent.InsufficientData(distinctDevices))
            return
        }

        val result = votingStrategy.computeConsensus(recentAssessments)

        when (result) {
            is ConsensusResult.Reached -> {
                Logger.d(TAG, "Consensus reached: ${result.crisisType} (${result.agreementRatio * 100}% agreement)")
                _consensusEvents.emit(ConsensusEvent.ConsensusReached(result, recentAssessments))
            }
            is ConsensusResult.Disputed -> {
                Logger.d(TAG, "Consensus disputed: ${result.topCrisisType} (${result.agreementRatio * 100}% agreement)")
                _consensusEvents.emit(ConsensusEvent.ConsensusDisputed(result))
            }
            ConsensusResult.NoConsensus -> {
                Logger.d(TAG, "No consensus yet (${recentAssessments.size} assessments)")
                _consensusEvents.emit(ConsensusEvent.InsufficientData(recentAssessments.size))
            }
        }
    }

    fun buildConsensusAssessment(
        result: ConsensusResult.Reached,
        assessments: List<SituationAssessment>
    ): SituationAssessment {
        val best = assessments.maxByOrNull { it.confidence }!!
        return best.copy(
            crisisType = result.crisisType,
            severity = result.severity,
            confidence = result.confidence,
            immediateRisks = result.immediateRisks,
            consensusContributors = result.contributingDevices,
            meshConsensus = true
        )
    }

    fun pruneStaleAssessments() {
        val cutoff = System.currentTimeMillis() - CONSENSUS_WINDOW_MS
        pendingAssessments.entries.removeIf { it.value.timestamp < cutoff }
    }

    fun getPendingCount(): Int = pendingAssessments.size

    companion object {
        private const val TAG = "ConsensusEngine"
        private const val CONSENSUS_WINDOW_MS = 30_000L
    }
}

sealed class ConsensusEvent {
    data class ConsensusReached(
        val result: ConsensusResult.Reached,
        val assessments: List<SituationAssessment>
    ) : ConsensusEvent()

    data class ConsensusDisputed(val result: ConsensusResult.Disputed) : ConsensusEvent()
    data class InsufficientData(val assessmentCount: Int) : ConsensusEvent()
}
