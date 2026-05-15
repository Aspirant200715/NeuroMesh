package com.neuromesh.crisis.infrastructure.sensor

/**
 * Raw, numeric signal snapshot used by the detection gate to decide whether the
 * (expensive) LLM pipeline should run at all. This is intentionally separate from
 * the human-readable describe* strings: text descriptions are for the model,
 * these numbers are for the gate.
 */
data class CameraSignal(
    val available: Boolean,
    val fireColorRatio: Float,   // fraction of red/orange-dominant pixels
    val floodColorRatio: Float,  // fraction of strongly blue pixels
    val smokeRatio: Float,       // fraction of grey/amber low-saturation pixels
    val brightRatio: Float,
    val darkRatio: Float
)

data class AudioSignal(
    val available: Boolean,
    val dbSpl: Float,
    val dominantFreq: Float,
    val peakAmplitude: Int
)

data class EnvironmentalSignal(
    val accelMagnitude: Float,
    val accelPeak: Float,
    val isSeismic: Boolean
)

/**
 * Whole-device sensor snapshot plus the gate decision.
 *
 * [crossesThreshold] is true only when at least one sensor shows a signal that
 * is genuinely out of the ordinary. When it is false, the detection pipeline
 * MUST short-circuit and not invoke the model — this is what eliminates the
 * "random alerts with no activity" bug and the constant-inference overheating.
 */
data class SensorSnapshot(
    val camera: CameraSignal,
    val audio: AudioSignal,
    val environmental: EnvironmentalSignal
) {
    val triggerReasons: List<String> = buildList {
        if (camera.available && camera.fireColorRatio >= FIRE_COLOR_GATE)
            add("strong fire-colored pixels (${(camera.fireColorRatio * 100).toInt()}%)")
        if (camera.available && camera.floodColorRatio >= FLOOD_COLOR_GATE)
            add("strong flood-colored pixels (${(camera.floodColorRatio * 100).toInt()}%)")
        if (camera.available && camera.smokeRatio >= SMOKE_GATE && camera.darkRatio >= 0.35f)
            add("smoke/obscured scene")
        if (audio.available && audio.dbSpl >= LOUD_DB_GATE &&
            audio.dominantFreq in ALARM_FREQ_MIN..ALARM_FREQ_MAX && audio.peakAmplitude >= ALARM_PEAK_GATE)
            add("loud alarm-band audio (${audio.dbSpl.toInt()}dB)")
        if (audio.available && audio.dbSpl >= SCREAM_DB_GATE &&
            audio.dominantFreq in SCREAM_FREQ_MIN..SCREAM_FREQ_MAX)
            add("loud voices/screaming (${audio.dbSpl.toInt()}dB)")
        if (audio.available && audio.dbSpl >= RUMBLE_DB_GATE && audio.dominantFreq < RUMBLE_FREQ_MAX)
            add("low-frequency rumble (${audio.dbSpl.toInt()}dB)")
        if (environmental.isSeismic)
            add("seismic accelerometer spike (${"%.1f".format(environmental.accelPeak)} m/s²)")
    }

    val crossesThreshold: Boolean get() = triggerReasons.isNotEmpty()

    companion object {
        // Camera gates — deliberately conservative so a normally-lit room never trips them.
        const val FIRE_COLOR_GATE = 0.22f
        const val FLOOD_COLOR_GATE = 0.35f
        const val SMOKE_GATE = 0.30f

        // Audio gates.
        const val LOUD_DB_GATE = 82f
        const val ALARM_FREQ_MIN = 800f
        const val ALARM_FREQ_MAX = 4000f
        const val ALARM_PEAK_GATE = 18000
        const val SCREAM_DB_GATE = 84f
        const val SCREAM_FREQ_MIN = 300f
        const val SCREAM_FREQ_MAX = 3400f
        const val RUMBLE_DB_GATE = 86f
        const val RUMBLE_FREQ_MAX = 110f
    }
}
