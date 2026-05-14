package com.neuromesh.crisis.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MeshPeer(
    val deviceId: String,
    val displayName: String,
    val endpointId: String,
    val connectionState: ConnectionState,
    val lastSeen: Long,
    val signalStrength: Int = -1,
    val batteryLevel: Int = -1,
    val capabilities: DeviceCapabilities = DeviceCapabilities()
)

@Serializable
enum class ConnectionState {
    DISCOVERED,
    CONNECTING,
    CONNECTED,
    DISCONNECTED,
    LOST
}

@Serializable
data class DeviceCapabilities(
    val hasCamera: Boolean = true,
    val hasMicrophone: Boolean = true,
    val hasAccelerometer: Boolean = true,
    val modelLoaded: Boolean = false,
    val availableRamMb: Int = 0
)

@Serializable
data class MeshMessage(
    val type: MessageType,
    val senderId: String,
    val timestamp: Long,
    val payload: String,
    val ttl: Int = 3
)

@Serializable
enum class MessageType {
    OBSERVATION,
    ASSESSMENT,
    ALERT,
    CONSENSUS_VOTE,
    PEER_HEARTBEAT,
    MODEL_READY,
    ACK
}
