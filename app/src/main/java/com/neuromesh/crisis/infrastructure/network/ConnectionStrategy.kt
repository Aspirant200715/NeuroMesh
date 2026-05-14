package com.neuromesh.crisis.infrastructure.network

import com.neuromesh.crisis.data.model.MeshPeer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionStrategy @Inject constructor() {

    fun shouldConnect(peer: MeshPeer, currentPeerCount: Int): Boolean {
        if (currentPeerCount >= MAX_PEERS) return false
        if (peer.capabilities.availableRamMb > 0 && peer.capabilities.availableRamMb < MIN_RAM_MB) return false
        return true
    }

    fun prioritizePeers(peers: List<MeshPeer>): List<MeshPeer> {
        return peers.sortedWith(
            compareByDescending<MeshPeer> { it.capabilities.modelLoaded }
                .thenByDescending { it.capabilities.availableRamMb }
                .thenByDescending { it.signalStrength }
        )
    }

    companion object {
        const val MAX_PEERS = 5
        const val MIN_RAM_MB = 512
    }
}
