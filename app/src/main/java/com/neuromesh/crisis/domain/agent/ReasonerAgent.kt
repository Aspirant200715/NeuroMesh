package com.neuromesh.crisis.domain.agent

import com.neuromesh.crisis.data.model.Observation
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
class ReasonerAgent @Inject constructor(
    private val modelRunner: Gemma4ModelRunner,
    private val promptBuilder: PromptBuilder,
    private val outputParser: OutputParser,
    private val deviceId: String
) {
    private val _assessments = MutableSharedFlow<SituationAssessment>(replay = 1)
    val assessments: Flow<SituationAssessment> = _assessments.asSharedFlow()

    suspend fun reason(observations: List<Observation>): Result<SituationAssessment> {
        if (observations.isEmpty()) {
            return Result.Error("No observations to reason about")
        }

        return try {
            val prompt = promptBuilder.buildReasonerPrompt(observations)

            Logger.d(TAG, "Running reasoner inference on ${observations.size} observations...")
            val start = System.currentTimeMillis()

            when (val result = modelRunner.generate(prompt)) {
                is Result.Success -> {
                    val latencyMs = System.currentTimeMillis() - start
                    Logger.d(TAG, "Reasoner inference completed in ${latencyMs}ms")

                    val observationIds = observations.map { it.id }
                    val assessment = outputParser.parseAssessment(deviceId, result.data, observationIds)
                        ?: return Result.Error("Failed to parse assessment output")

                    _assessments.emit(assessment)
                    Result.Success(assessment)
                }
                is Result.Error -> Result.Error(result.message)
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Reasoner agent error", e)
            Result.Error("Reasoner agent failed: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "ReasonerAgent"
    }
}