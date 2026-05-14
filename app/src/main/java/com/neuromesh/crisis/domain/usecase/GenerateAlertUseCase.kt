package com.neuromesh.crisis.domain.usecase

import com.neuromesh.crisis.data.local.dao.AlertDao
import com.neuromesh.crisis.data.local.entity.AlertEntity
import com.neuromesh.crisis.data.model.CrisisAlert
import com.neuromesh.crisis.data.model.SituationAssessment
import com.neuromesh.crisis.domain.agent.ActionAgent
import com.neuromesh.crisis.util.Logger
import com.neuromesh.crisis.util.Result
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class GenerateAlertUseCase @Inject constructor(
    private val actionAgent: ActionAgent,
    private val alertDao: AlertDao,
    private val json: Json
) {

    suspend operator fun invoke(assessment: SituationAssessment): Result<CrisisAlert> {
        return try {
            when (val result = actionAgent.generateAlert(assessment)) {
                is Result.Success -> {
                    val alert = result.data
                    persistAlert(alert)
                    Logger.d(TAG, "Alert generated: ${alert.title}")
                    Result.Success(alert)
                }
                is Result.Error -> result
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Generate alert failed", e)
            Result.Error("Failed to generate alert: ${e.message}")
        }
    }

    private suspend fun persistAlert(alert: CrisisAlert) {
        val entity = AlertEntity(
            id = alert.id,
            assessmentId = alert.assessmentId,
            timestamp = alert.timestamp,
            crisisType = alert.crisisType,
            severityValue = alert.severity.value,
            title = alert.title,
            summaryJson = json.encodeToString(alert),
            isConsensusAlert = alert.isConsensusAlert,
            contributingDevices = alert.contributingDevices,
            expiresAt = alert.expiresAt
        )
        alertDao.insert(entity)
    }

    companion object {
        private const val TAG = "GenerateAlertUseCase"
    }
}
