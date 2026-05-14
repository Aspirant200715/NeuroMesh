package com.neuromesh.crisis.domain.usecase

import com.neuromesh.crisis.data.model.MeshMessage
import com.neuromesh.crisis.data.model.MessageType
import com.neuromesh.crisis.data.model.Observation
import com.neuromesh.crisis.data.model.SituationAssessment
import com.neuromesh.crisis.domain.consensus.ConsensusEngine
import com.neuromesh.crisis.domain.consensus.GossipProtocol
import com.neuromesh.crisis.util.Logger
import com.neuromesh.crisis.util.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ShareObservationUseCase @Inject constructor(
    private val gossipProtocol: GossipProtocol,
    private val consensusEngine: ConsensusEngine,
    private val json: Json,
    private val deviceId: String
) {

    suspend fun shareObservation(observation: Observation): Result<Unit> {
        return try {
            val message = MeshMessage(
                type = MessageType.OBSERVATION,
                senderId = deviceId,
                timestamp = System.currentTimeMillis(),
                payload = json.encodeToString(observation)
            )
            gossipProtocol.broadcast(message)
            Logger.d(TAG, "Shared observation ${observation.id.take(8)} to mesh")
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to share observation: ${e.message}")
        }
    }

    suspend fun shareAssessment(assessment: SituationAssessment): Result<Unit> {
        return try {
            val message = MeshMessage(
                type = MessageType.ASSESSMENT,
                senderId = deviceId,
                timestamp = System.currentTimeMillis(),
                payload = json.encodeToString(assessment)
            )
            gossipProtocol.broadcast(message)
            consensusEngine.submitAssessment(assessment)
            Logger.d(TAG, "Shared assessment ${assessment.id.take(8)} to mesh")
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to share assessment: ${e.message}")
        }
    }

    suspend fun receiveObservation(observation: Observation) {
        Logger.d(TAG, "Received observation from ${observation.deviceId.take(8)}")
    }

    suspend fun receiveAssessment(
        assessment: SituationAssessment,
        scope: CoroutineScope
    ) {
        Logger.d(TAG, "Received assessment from ${assessment.deviceId.take(8)}")
        consensusEngine.submitAssessment(assessment)

        val relayMessage = MeshMessage(
            type = MessageType.ASSESSMENT,
            senderId = assessment.deviceId,
            timestamp = assessment.timestamp,
            payload = json.encodeToString(assessment)
        )
        gossipProtocol.relay(relayMessage, scope)
    }

    companion object {
        private const val TAG = "ShareObservationUseCase"
    }
}
