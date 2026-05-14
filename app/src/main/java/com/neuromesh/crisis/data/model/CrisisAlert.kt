package com.neuromesh.crisis.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CrisisAlert(
    val id: String,
    val assessmentId: String,
    val timestamp: Long,
    val crisisType: CrisisType,
    val severity: SeverityLevel,
    val title: String,
    val summary: String,
    val immediateActions: List<String>,
    val evacuationRoutes: List<String>,
    val doNotDo: List<String>,
    val contactNumbers: List<EmergencyContact>,
    val isConsensusAlert: Boolean,
    val contributingDevices: Int,
    val expiresAt: Long,
    val guidanceText: String,
    val reasoningVisible: Boolean = true
)

@Serializable
data class EmergencyContact(
    val name: String,
    val number: String,
    val available: Boolean = true
)
