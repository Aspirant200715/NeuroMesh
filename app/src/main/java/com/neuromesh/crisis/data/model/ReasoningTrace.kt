package com.neuromesh.crisis.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ReasoningTrace(
    val agentId: String,
    val agentType: AgentType,
    val timestamp: Long,
    val inputs: List<String>,
    val reasoning: String,
    val conclusion: String,
    val confidence: Float,
    val steps: List<ReasoningStep>
)

@Serializable
data class ReasoningStep(
    val stepNumber: Int,
    val description: String,
    val evidence: String,
    val confidence: Float
)

@Serializable
enum class AgentType {
    OBSERVER,
    REASONER,
    ACTION
}
