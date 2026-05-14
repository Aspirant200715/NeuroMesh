package com.neuromesh.crisis.infrastructure.ml

import com.neuromesh.crisis.data.model.*
import com.neuromesh.crisis.util.Logger
import kotlinx.serialization.json.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OutputParser @Inject constructor(private val json: Json) {

    fun parseObservation(deviceId: String, rawOutput: String): Observation? {
        return try {
            val jsonStr = extractJson(rawOutput) ?: return null
            val obj = json.parseToJsonElement(jsonStr).jsonObject

            val crisisType = obj["crisisType"]?.jsonPrimitive?.content
                ?.let { runCatching { CrisisType.valueOf(it) }.getOrDefault(CrisisType.UNKNOWN) }
                ?: CrisisType.UNKNOWN

            val confidence = obj["confidence"]?.jsonPrimitive?.float ?: 0f

            val visual = obj["visualFeatures"]?.jsonObject?.let { v ->
                VisualFeatures(
                    smokeDetected = v["smokeDetected"]?.jsonPrimitive?.boolean ?: false,
                    flamesDetected = v["flamesDetected"]?.jsonPrimitive?.boolean ?: false,
                    floodWaterDetected = v["floodWaterDetected"]?.jsonPrimitive?.boolean ?: false,
                    structuralDamageDetected = v["structuralDamageDetected"]?.jsonPrimitive?.boolean ?: false,
                    crowdPanic = v["crowdPanic"]?.jsonPrimitive?.boolean ?: false
                )
            }

            val audio = obj["audioFeatures"]?.jsonObject?.let { a ->
                AudioFeatures(
                    alarmDetected = a["alarmDetected"]?.jsonPrimitive?.boolean ?: false,
                    screamingDetected = a["screamingDetected"]?.jsonPrimitive?.boolean ?: false,
                    explosionDetected = a["explosionDetected"]?.jsonPrimitive?.boolean ?: false
                )
            }

            val indicators = obj["indicators"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
            val reasoning = obj["reasoning"]?.jsonPrimitive?.content ?: ""

            Observation(
                id = generateId(),
                deviceId = deviceId,
                timestamp = System.currentTimeMillis(),
                crisisType = crisisType,
                confidence = confidence,
                visualFeatures = visual,
                audioFeatures = audio,
                rawText = "$reasoning | Indicators: ${indicators.joinToString(", ")}"
            )
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to parse observation: ${e.message}")
            null
        }
    }

    fun parseAssessment(deviceId: String, rawOutput: String, observationIds: List<String>): SituationAssessment? {
        return try {
            val jsonStr = extractJson(rawOutput) ?: return null
            val obj = json.parseToJsonElement(jsonStr).jsonObject

            val crisisType = obj["crisisType"]?.jsonPrimitive?.content
                ?.let { runCatching { CrisisType.valueOf(it) }.getOrDefault(CrisisType.UNKNOWN) }
                ?: CrisisType.UNKNOWN

            val severity = obj["severity"]?.jsonPrimitive?.content
                ?.let { runCatching { SeverityLevel.valueOf(it) }.getOrDefault(SeverityLevel.LOW) }
                ?: SeverityLevel.LOW

            val confidence = obj["confidence"]?.jsonPrimitive?.float ?: 0f

            val steps = obj["steps"]?.jsonArray?.mapIndexed { i, step ->
                val s = step.jsonObject
                ReasoningStep(
                    stepNumber = s["stepNumber"]?.jsonPrimitive?.int ?: (i + 1),
                    description = s["description"]?.jsonPrimitive?.content ?: "",
                    evidence = s["evidence"]?.jsonPrimitive?.content ?: "",
                    confidence = s["confidence"]?.jsonPrimitive?.float ?: 0f
                )
            } ?: emptyList()

            val trace = ReasoningTrace(
                agentId = deviceId,
                agentType = AgentType.REASONER,
                timestamp = System.currentTimeMillis(),
                inputs = observationIds,
                reasoning = obj["conclusion"]?.jsonPrimitive?.content ?: "",
                conclusion = obj["conclusion"]?.jsonPrimitive?.content ?: "",
                confidence = confidence,
                steps = steps
            )

            SituationAssessment(
                id = generateId(),
                deviceId = deviceId,
                timestamp = System.currentTimeMillis(),
                crisisType = crisisType,
                severity = severity,
                confidence = confidence,
                affectedArea = obj["affectedArea"]?.jsonPrimitive?.content ?: "Unknown area",
                estimatedAffected = obj["estimatedAffected"]?.jsonPrimitive?.int ?: 0,
                immediateRisks = obj["immediateRisks"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
                observations = observationIds,
                reasoningTrace = trace
            )
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to parse assessment: ${e.message}")
            null
        }
    }

    fun parseAlert(assessmentId: String, rawOutput: String): CrisisAlert? {
        return try {
            val jsonStr = extractJson(rawOutput) ?: return null
            val obj = json.parseToJsonElement(jsonStr).jsonObject

            val contacts = obj["contacts"]?.jsonArray?.map { c ->
                val contact = c.jsonObject
                EmergencyContact(
                    name = contact["name"]?.jsonPrimitive?.content ?: "",
                    number = contact["number"]?.jsonPrimitive?.content ?: ""
                )
            } ?: listOf(EmergencyContact("Emergency Services", "911"))

            CrisisAlert(
                id = generateId(),
                assessmentId = assessmentId,
                timestamp = System.currentTimeMillis(),
                crisisType = CrisisType.UNKNOWN,
                severity = SeverityLevel.HIGH,
                title = obj["title"]?.jsonPrimitive?.content ?: "Crisis Detected",
                summary = obj["summary"]?.jsonPrimitive?.content ?: "",
                immediateActions = obj["immediateActions"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
                evacuationRoutes = obj["evacuationRoutes"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
                doNotDo = obj["doNotDo"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
                contactNumbers = contacts,
                isConsensusAlert = false,
                contributingDevices = 1,
                expiresAt = System.currentTimeMillis() + ALERT_TTL_MS,
                guidanceText = obj["guidanceText"]?.jsonPrimitive?.content ?: ""
            )
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to parse alert: ${e.message}")
            null
        }
    }

    private fun extractJson(text: String): String? {
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        if (start == -1 || end == -1 || end <= start) return null
        return text.substring(start, end + 1)
    }

    private fun generateId(): String =
        "${System.currentTimeMillis()}_${(1000..9999).random()}"

    companion object {
        private const val TAG = "OutputParser"
        private const val ALERT_TTL_MS = 30 * 60 * 1000L
    }
}
