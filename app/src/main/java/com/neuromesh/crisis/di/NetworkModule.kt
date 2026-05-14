package com.neuromesh.crisis.di

import com.neuromesh.crisis.domain.agent.ActionAgent
import com.neuromesh.crisis.domain.agent.ObserverAgent
import com.neuromesh.crisis.domain.agent.ReasonerAgent
import com.neuromesh.crisis.infrastructure.ml.Gemma4ModelRunner
import com.neuromesh.crisis.infrastructure.ml.OutputParser
import com.neuromesh.crisis.infrastructure.ml.PromptBuilder
import com.neuromesh.crisis.infrastructure.sensor.AudioSensorManager
import com.neuromesh.crisis.infrastructure.sensor.CameraSensorManager
import com.neuromesh.crisis.infrastructure.sensor.EnvironmentalSensorManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideObserverAgent(
        modelRunner: Gemma4ModelRunner,
        promptBuilder: PromptBuilder,
        outputParser: OutputParser,
        cameraSensor: CameraSensorManager,
        audioSensor: AudioSensorManager,
        envSensor: EnvironmentalSensorManager,
        deviceId: String
    ): ObserverAgent = ObserverAgent(
        modelRunner, promptBuilder, outputParser, cameraSensor, audioSensor, envSensor, deviceId
    )

    @Provides
    @Singleton
    fun provideReasonerAgent(
        modelRunner: Gemma4ModelRunner,
        promptBuilder: PromptBuilder,
        outputParser: OutputParser,
        deviceId: String
    ): ReasonerAgent = ReasonerAgent(modelRunner, promptBuilder, outputParser, deviceId)

    @Provides
    @Singleton
    fun provideActionAgent(
        modelRunner: Gemma4ModelRunner,
        promptBuilder: PromptBuilder,
        outputParser: OutputParser
    ): ActionAgent = ActionAgent(modelRunner, promptBuilder, outputParser)
}
