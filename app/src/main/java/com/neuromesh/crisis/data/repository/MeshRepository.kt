package com.neuromesh.crisis.data.repository

import com.neuromesh.crisis.data.model.MeshPeer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeshRepository @Inject constructor() {

    private val _peers = MutableStateFlow<Map<String, MeshPeer>>(emptyMap())

    val peers: Flow<List<MeshPeer>> = _peers.asStateFlow().map { it.values.toList() }
    val connectedPeers: Flow<List<MeshPeer>> = peers.map { list ->
        list.filter { it.connectionState == com.neuromesh.crisis.data.model.ConnectionState.CONNECTED }
    }

    fun addOrUpdatePeer(peer: MeshPeer) {
        _peers.value = _peers.value.toMutableMap().also { it[peer.deviceId] = peer }
    }

    fun removePeer(deviceId: String) {
        _peers.value = _peers.value.toMutableMap().also { it.remove(deviceId) }
    }

    fun getPeer(deviceId: String): MeshPeer? = _peers.value[deviceId]

    fun getConnectedPeerCount(): Int =
        _peers.value.values.count { it.connectionState == com.neuromesh.crisis.data.model.ConnectionState.CONNECTED }

    fun getAllPeers(): List<MeshPeer> = _peers.value.values.toList()

    fun getConnectedPeers(): List<MeshPeer> =
        _peers.value.values.filter { it.connectionState == com.neuromesh.crisis.data.model.ConnectionState.CONNECTED }

    fun updateConnectionState(deviceId: String, state: com.neuromesh.crisis.data.model.ConnectionState) {
        val existing = _peers.value[deviceId] ?: return
        addOrUpdatePeer(existing.copy(connectionState = state, lastSeen = System.currentTimeMillis()))
    }

    fun clear() {
        _peers.value = emptyMap()
    }
}
