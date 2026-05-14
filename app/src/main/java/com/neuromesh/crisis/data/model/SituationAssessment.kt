package com.neuromesh.crisis.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SituationAssessment(
    val id: String,
    val deviceId: String,
    val timestamp: Long,
    val crisisType: CrisisType,
    val severity: SeverityLevel,
    val confidence: Float,
    val affectedArea: String,
    val estimatedAffected: Int,
    val immediateRisks: List<String>,
    val observations: List<String>,
    val reasoningTrace: ReasoningTrace,
    val consensusContributors: List<String> = emptyList(),
    val meshConsensus: Boolean = false
)

@Serializable
enum class SeverityLevel(val value: Int) {
    LOW(1),
    MODERATE(2),
    HIGH(3),
    CRITICAL(4),
    CATASTROPHIC(5)
}
