package com.neuromesh.crisis.domain.agent

import com.neuromesh.crisis.data.model.Observation
import com.neuromesh.crisis.infrastructure.ml.Gemma4ModelRunner
import com.neuromesh.crisis.infrastructure.ml.OutputParser
import com.neuromesh.crisis.infrastructure.ml.PromptBuilder
import com.neuromesh.crisis.infrastructure.sensor.AudioSensorManager
import com.neuromesh.crisis.infrastructure.sensor.CameraSensorManager
import com.neuromesh.crisis.infrastructure.sensor.EnvironmentalSensorManager
import com.neuromesh.crisis.util.Logger
import com.neuromesh.crisis.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserverAgent @Inject constructor(
    private val modelRunner: Gemma4ModelRunner,
    private val promptBuilder: PromptBuilder,
    private val outputParser: OutputParser,
    private val cameraSensor: CameraSensorManager,
    private val audioSensor: AudioSensorManager,
    private val envSensor: EnvironmentalSensorManager,
    private val deviceId: String
) {
    private val _observations = MutableSharedFlow<Observation>(replay = 1)
    val observations: Flow<Observation> = _observations.asSharedFlow()

    suspend fun analyze(): Result<Observation> {
        return try {
            val imageDesc = cameraSensor.captureAndDescribe()
            val audioDesc = audioSensor.recordAndDescribe()
            val sensorData = envSensor.readSensors()

            val prompt = promptBuilder.buildObserverPrompt(
                imageDescription = imageDesc,
                audioDescription = audioDesc,
                sensorData = sensorData
            )

            Logger.d(TAG, "Running observer inference...")
            val start = System.currentTimeMillis()

            when (val result = modelRunner.generate(prompt)) {
                is Result.Success -> {
                    val latencyMs = System.currentTimeMillis() - start
                    Logger.d(TAG, "Observer inference completed in ${latencyMs}ms")

                    val observation = outputParser.parseObservation(deviceId, result.data)
                        ?: return Result.Error("Failed to parse observation output")

                    _observations.emit(observation)
                    Result.Success(observation)
                }
                is Result.Error -> Result.Error(result.message)
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Observer agent error", e)
            Result.Error("Observer agent failed: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "ObserverAgent"
    }
}