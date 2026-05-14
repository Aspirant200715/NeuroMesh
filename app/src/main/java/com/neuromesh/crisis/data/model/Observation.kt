package com.neuromesh.crisis.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Observation(
    val id: String,
    val deviceId: String,
    val timestamp: Long,
    val crisisType: CrisisType,
    val confidence: Float,
    val visualFeatures: VisualFeatures? = null,
    val audioFeatures: AudioFeatures? = null,
    val sensorFeatures: SensorFeatures? = null,
    val rawText: String = "",
    val location: DeviceLocation? = null
)

@Serializable
enum class CrisisType {
    FIRE,
    EARTHQUAKE,
    FLOOD,
    STRUCTURAL_COLLAPSE,
    MEDICAL_EMERGENCY,
    UNKNOWN
}

@Serializable
data class VisualFeatures(
    val smokeDetected: Boolean = false,
    val flamesDetected: Boolean = false,
    val floodWaterDetected: Boolean = false,
    val structuralDamageDetected: Boolean = false,
    val crowdPanic: Boolean = false,
    val visibilityScore: Float = 1.0f,
    val dominantColors: List<String> = emptyList(),
    val objectsDetected: List<String> = emptyList()
)

@Serializable
data class AudioFeatures(
    val alarmDetected: Boolean = false,
    val screamingDetected: Boolean = false,
    val explosionDetected: Boolean = false,
    val glassBreakingDetected: Boolean = false,
    val ambientNoiseLevel: Float = 0f,
    val peakFrequency: Float = 0f
)

@Serializable
data class SensorFeatures(
    val accelerometerMagnitude: Float = 0f,
    val isSeismicActivity: Boolean = false,
    val temperatureAnomalous: Boolean = false,
    val pressureAnomalous: Boolean = false,
    val humidity: Float = 0f
)

@Serializable
data class DeviceLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val accuracy: Float = 0f
)
