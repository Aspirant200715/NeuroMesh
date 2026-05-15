package com.neuromesh.crisis.domain.agent

import com.neuromesh.crisis.data.model.AudioFeatures
import com.neuromesh.crisis.data.model.CrisisType
import com.neuromesh.crisis.data.model.Observation
import com.neuromesh.crisis.data.model.SensorFeatures
import com.neuromesh.crisis.data.model.VisualFeatures
import com.neuromesh.crisis.infrastructure.sensor.AudioSensorManager
import com.neuromesh.crisis.infrastructure.sensor.CameraSensorManager
import com.neuromesh.crisis.infrastructure.sensor.EnvironmentalSensorManager
import com.neuromesh.crisis.infrastructure.sensor.SensorSnapshot
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads the raw sensor numbers and answers two questions:
 *
 *  1. [snapshot] — should the (expensive) LLM pipeline run at all? This is the
 *     gate that fixes both the random-alert bug and the overheating: if no
 *     sensor crosses a real threshold, the caller skips inference entirely.
 *
 *  2. [classify] — a pure rule-based classifier used as the fallback on phones
 *     that cannot host the LLM (low RAM). It produces a real [Observation] from
 *     the same numbers so those phones still detect crises and feed the mesh.
 */
@Singleton
class HeuristicDetector @Inject constructor(
    private val cameraSensor: CameraSensorManager,
    private val audioSensor: AudioSensorManager,
    private val envSensor: EnvironmentalSensorManager,
    private val deviceId: String
) {

    suspend fun snapshot(): SensorSnapshot = SensorSnapshot(
        camera = cameraSensor.readSignal(),
        audio = audioSensor.readSignal(),
        environmental = envSensor.readSignal()
    )

    /**
     * Rule-based crisis classification. Returns null when nothing meaningful is
     * present (caller treats that as "no crisis", never an alert).
     */
    fun classify(snapshot: SensorSnapshot): Observation? {
        val cam = snapshot.camera
        val aud = snapshot.audio
        val env = snapshot.environmental

        var type = CrisisType.UNKNOWN
        var confidence = 0f

        // Earthquake — accelerometer is the most reliable single signal.
        if (env.isSeismic) {
            type = CrisisType.EARTHQUAKE
            confidence = ((env.accelPeak - 15f) / 15f).coerceIn(0.6f, 0.95f)
        }

        // Fire — strong fire-colored pixels, optionally corroborated by an alarm.
        val alarm = aud.available && aud.dbSpl >= 82f &&
            aud.dominantFreq in 800f..4000f && aud.peakAmplitude >= 18000
        if (cam.available && cam.fireColorRatio >= SensorSnapshot.FIRE_COLOR_GATE) {
            val fireConf = (0.55f + cam.fireColorRatio).coerceAtMost(0.9f) +
                if (alarm) 0.05f else 0f
            if (fireConf > confidence) {
                type = CrisisType.FIRE
                confidence = fireConf.coerceAtMost(0.95f)
            }
        }

        // Flood — strong blue-dominant scene.
        if (cam.available && cam.floodColorRatio >= SensorSnapshot.FLOOD_COLOR_GATE) {
            val floodConf = (0.5f + cam.floodColorRatio).coerceAtMost(0.85f)
            if (floodConf > confidence) {
                type = CrisisType.FLOOD
                confidence = floodConf
            }
        }

        if (type == CrisisType.UNKNOWN || confidence < 0.5f) return null

        return Observation(
            id = "${System.currentTimeMillis()}_${(1000..9999).random()}",
            deviceId = deviceId,
            timestamp = System.currentTimeMillis(),
            crisisType = type,
            confidence = confidence,
            visualFeatures = VisualFeatures(
                flamesDetected = type == CrisisType.FIRE,
                floodWaterDetected = type == CrisisType.FLOOD,
                smokeDetected = cam.smokeRatio >= SensorSnapshot.SMOKE_GATE
            ),
            audioFeatures = AudioFeatures(alarmDetected = alarm),
            sensorFeatures = SensorFeatures(
                accelerometerMagnitude = env.accelPeak,
                isSeismicActivity = env.isSeismic
            ),
            rawText = "Heuristic detection (no on-device LLM). " +
                "Triggers: ${snapshot.triggerReasons.joinToString("; ")}"
        )
    }
}
