package com.neuromesh.crisis.infrastructure.network

import com.neuromesh.crisis.data.model.*
import com.neuromesh.crisis.util.Logger
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageSerializer @Inject constructor(private val json: Json) {

    fun serialize(message: MeshMessage): ByteArray {
        return json.encodeToString(message).toByteArray(Charsets.UTF_8)
    }

    fun deserialize(bytes: ByteArray): MeshMessage? {
        return try {
            val text = bytes.toString(Charsets.UTF_8)
            json.decodeFromString(text)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to deserialize message: ${e.message}")
            null
        }
    }

    fun decodeObservation(payload: String): Observation? {
        return try {
            json.decodeFromString(payload)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to decode observation: ${e.message}")
            null
        }
    }

    fun decodeAssessment(payload: String): SituationAssessment? {
        return try {
            json.decodeFromString(payload)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to decode assessment: ${e.message}")
            null
        }
    }

    fun decodeAlert(payload: String): CrisisAlert? {
        return try {
            json.decodeFromString(payload)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to decode alert: ${e.message}")
            null
        }
    }

    companion object {
        private const val TAG = "MessageSerializer"
    }
}
