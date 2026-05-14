package com.neuromesh.crisis.infrastructure.ml

import com.neuromesh.crisis.data.model.Observation
import com.neuromesh.crisis.data.model.SituationAssessment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromptBuilder @Inject constructor() {

    fun buildObserverPrompt(
        imageDescription: String,
        audioDescription: String,
        sensorData: String
    ): String = buildString {
        append(SYSTEM_OBSERVER)
        append("\n\n")
        append("## Sensor Data\n")
        append("**Visual Input:** $imageDescription\n")
        append("**Audio Input:** $audioDescription\n")
        append("**Sensor Readings:** $sensorData\n\n")
        append("## Task\n")
        append("Analyze the above sensor data and identify any signs of crisis (fire, earthquake, flood, structural collapse, medical emergency).\n\n")
        append("Respond ONLY with this exact JSON structure:\n")
        append(OBSERVER_OUTPUT_SCHEMA)
    }

    fun buildReasonerPrompt(observations: List<Observation>): String = buildString {
        append(SYSTEM_REASONER)
        append("\n\n")
        append("## Observations from ${observations.size} source(s)\n")
        observations.forEachIndexed { i, obs ->
            append("**Observation ${i + 1}** (device: ${obs.deviceId.take(8)}, confidence: ${obs.confidence}):\n")
            append("- Crisis type detected: ${obs.crisisType}\n")
            append("- Raw analysis: ${obs.rawText}\n")
            obs.visualFeatures?.let { append("- Visual: smoke=${it.smokeDetected}, flames=${it.flamesDetected}, flood=${it.floodWaterDetected}\n") }
            obs.audioFeatures?.let { append("- Audio: alarm=${it.alarmDetected}, screaming=${it.screamingDetected}\n") }
            obs.sensorFeatures?.let { append("- Seismic: ${it.isSeismicActivity}, magnitude: ${it.accelerometerMagnitude}\n") }
        }
        append("\n## Task\n")
        append("Synthesize these observations into a coherent situation assessment. Show your reasoning step by step.\n\n")
        append("Respond ONLY with this exact JSON structure:\n")
        append(REASONER_OUTPUT_SCHEMA)
    }

    fun buildActionPrompt(assessment: SituationAssessment): String = buildString {
        append(SYSTEM_ACTION)
        append("\n\n")
        append("## Situation Assessment\n")
        append("- Crisis type: ${assessment.crisisType}\n")
        append("- Severity: ${assessment.severity}\n")
        append("- Confidence: ${assessment.confidence}\n")
        append("- Affected area: ${assessment.affectedArea}\n")
        append("- Immediate risks: ${assessment.immediateRisks.joinToString(", ")}\n\n")
        append("## Task\n")
        append("Generate specific, actionable emergency guidance for people in this situation.\n\n")
        append("Respond ONLY with this exact JSON structure:\n")
        append(ACTION_OUTPUT_SCHEMA)
    }

    companion object {
        private const val SYSTEM_OBSERVER = """<start_of_turn>user
You are the Observer Agent in the NeuroMesh crisis detection system.
Your role: analyze raw sensor data and identify crisis indicators.
Be precise, concise, and output only valid JSON.
IMPORTANT: This runs offline on a phone. Be efficient and accurate."""

        private const val SYSTEM_REASONER = """<start_of_turn>user
You are the Reasoner Agent in the NeuroMesh crisis detection system.
Your role: synthesize observations from multiple sensors/devices into a coherent situation assessment.
Use evidence-based reasoning. Show your work step by step.
Output only valid JSON."""

        private const val SYSTEM_ACTION = """<start_of_turn>user
You are the Action Agent in the NeuroMesh crisis detection system.
Your role: generate specific, actionable emergency guidance based on a crisis assessment.
Prioritize life safety. Be clear and specific. Avoid panic.
Output only valid JSON."""

        const val OBSERVER_OUTPUT_SCHEMA = """{
  "crisisType": "FIRE|EARTHQUAKE|FLOOD|STRUCTURAL_COLLAPSE|MEDICAL_EMERGENCY|UNKNOWN",
  "confidence": 0.0,
  "indicators": ["list of observed indicators"],
  "visualFeatures": {"smokeDetected": false, "flamesDetected": false, "floodWaterDetected": false, "structuralDamageDetected": false, "crowdPanic": false},
  "audioFeatures": {"alarmDetected": false, "screamingDetected": false, "explosionDetected": false},
  "reasoning": "brief explanation"
}<end_of_turn>
<start_of_turn>model"""

        const val REASONER_OUTPUT_SCHEMA = """{
  "crisisType": "FIRE|EARTHQUAKE|FLOOD|STRUCTURAL_COLLAPSE|MEDICAL_EMERGENCY|UNKNOWN",
  "severity": "LOW|MODERATE|HIGH|CRITICAL|CATASTROPHIC",
  "confidence": 0.0,
  "affectedArea": "description",
  "estimatedAffected": 0,
  "immediateRisks": ["risk1", "risk2"],
  "steps": [
    {"stepNumber": 1, "description": "...", "evidence": "...", "confidence": 0.0}
  ],
  "conclusion": "summary"
}<end_of_turn>
<start_of_turn>model"""

        const val ACTION_OUTPUT_SCHEMA = """{
  "title": "Alert title (max 60 chars)",
  "summary": "1-2 sentence summary",
  "immediateActions": ["action1", "action2", "action3"],
  "evacuationRoutes": ["route1", "route2"],
  "doNotDo": ["don't do this1", "don't do this2"],
  "guidanceText": "paragraph of detailed guidance",
  "contacts": [{"name": "Emergency Services", "number": "911"}]
}<end_of_turn>
<start_of_turn>model"""
    }
}