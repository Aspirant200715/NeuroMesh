package com.neuromesh.crisis.domain.agent

import com.neuromesh.crisis.data.model.CrisisAlert
import com.neuromesh.crisis.data.model.SituationAssessment
import com.neuromesh.crisis.infrastructure.ml.Gemma4ModelRunner
import com.neuromesh.crisis.infrastructure.ml.OutputParser
import com.neuromesh.crisis.infrastructure.ml.PromptBuilder
import com.neuromesh.crisis.util.Logger
import com.neuromesh.crisis.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionAgent @Inject constructor(
    private val modelRunner: Gemma4ModelRunner,
    private val promptBuilder: PromptBuilder,
    private val outputParser: OutputParser
) {
    private val _alerts = MutableSharedFlow<CrisisAlert>(replay = 1)
    val alerts: Flow<CrisisAlert> = _alerts.asSharedFlow()

    suspend fun generateAlert(assessment: SituationAssessment): Result<CrisisAlert> {
        return try {
            val prompt = promptBuilder.buildActionPrompt(assessment)

            Logger.d(TAG, "Running action inference for ${assessment.crisisType}...")
            val start = System.currentTimeMillis()

            when (val result = modelRunner.generate(prompt)) {
                is Result.Success -> {
                    val latencyMs = System.currentTimeMillis() - start
                    Logger.d(TAG, "Action inference completed in ${latencyMs}ms")

                    val alert = outputParser.parseAlert(assessment.id, result.data)
                        ?: return Result.Error("Failed to parse alert output")

                    val enrichedAlert = alert.copy(
                        crisisType = assessment.crisisType,
                        severity = assessment.severity,
                        isConsensusAlert = assessment.meshConsensus,
                        contributingDevices = assessment.consensusContributors.size.coerceAtLeast(1)
                    )

                    _alerts.emit(enrichedAlert)
                    Result.Success(enrichedAlert)
                }
                is Result.Error -> Result.Error(result.message)
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Action agent error", e)
            Result.Error("Action agent failed: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "ActionAgent"
    }
}