package com.neuromesh.crisis.infrastructure.sensor

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.neuromesh.crisis.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.sqrt

@Singleton
class AudioSensorManager @Inject constructor() {

    private var isRecording = false

    suspend fun recordAndDescribe(): String = withContext(Dispatchers.IO) {
        try {
            val bufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(DEFAULT_BUFFER_SIZE)

            val recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                return@withContext "Microphone unavailable"
            }

            val buffer = ShortArray(bufferSize / 2)
            recorder.startRecording()
            isRecording = true

            recorder.read(buffer, 0, buffer.size)
            recorder.stop()
            recorder.release()
            isRecording = false

            analyzeAudio(buffer)
        } catch (e: SecurityException) {
            Logger.w(TAG, "Audio permission denied")
            "Audio permission not granted"
        } catch (e: Exception) {
            Logger.e(TAG, "Audio recording failed", e)
            "Audio analysis unavailable"
        }
    }

    private fun analyzeAudio(samples: ShortArray): String {
        if (samples.isEmpty()) return "No audio data"

        val rms = sqrt(samples.map { it.toLong() * it }.average()).toFloat()
        val dbSpl = if (rms > 0) 20 * log10(rms / Short.MAX_VALUE.toFloat()) + 90f else 0f

        var zeroCrossings = 0
        for (i in 1 until samples.size) {
            if ((samples[i] >= 0) != (samples[i - 1] >= 0)) zeroCrossings++
        }
        val dominantFreq = (zeroCrossings.toFloat() / samples.size) * SAMPLE_RATE / 2

        val peakAmplitude = samples.maxOfOrNull { abs(it.toInt()) } ?: 0

        val description = StringBuilder()

        when {
            dbSpl > 90 -> description.append("Very loud noise (${dbSpl.toInt()}dB). ")
            dbSpl > 75 -> description.append("Elevated noise level (${dbSpl.toInt()}dB). ")
            dbSpl > 60 -> description.append("Moderate noise (${dbSpl.toInt()}dB). ")
            else -> description.append("Quiet environment (${dbSpl.toInt()}dB). ")
        }

        when {
            dominantFreq in 300f..3400f && dbSpl > 80 ->
                description.append("Possible human voices/screaming. ")
            dominantFreq < 100f && dbSpl > 85 ->
                description.append("Low-frequency rumbling - possible earthquake/explosion. ")
            dominantFreq in 800f..4000f && peakAmplitude > 20000 ->
                description.append("High-pitched alarm pattern detected. ")
        }

        if (peakAmplitude > 30000) {
            description.append("Sudden loud impact/explosion detected. ")
        }

        return description.toString().trim()
    }

    companion object {
        private const val TAG = "AudioSensorManager"
        private const val SAMPLE_RATE = 44100
        private const val DEFAULT_BUFFER_SIZE = 8192
    }
}
