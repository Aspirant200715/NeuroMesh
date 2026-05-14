package com.neuromesh.crisis.domain.consensus

import com.neuromesh.crisis.data.model.MeshMessage
import com.neuromesh.crisis.data.model.MessageType
import com.neuromesh.crisis.infrastructure.network.MeshNetworkManager
import com.neuromesh.crisis.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GossipProtocol @Inject constructor(
    private val networkManager: MeshNetworkManager,
    private val json: Json
) {
    private val seenMessages = LinkedHashSet<String>()

    suspend fun broadcast(message: MeshMessage) {
        val messageKey = "${message.senderId}_${message.timestamp}_${message.type}"
        if (seenMessages.contains(messageKey)) return

        seenMessages.add(messageKey)
        if (seenMessages.size > MAX_SEEN_CACHE) {
            seenMessages.iterator().let { it.next(); it.remove() }
        }

        val payload = json.encodeToString(message)
        networkManager.broadcastToAll(payload)
        Logger.d(TAG, "Gossip broadcast: ${message.type} from ${message.senderId.take(8)}")
    }

    suspend fun relay(message: MeshMessage, scope: CoroutineScope) {
        if (message.ttl <= 0) return

        val relayMsg = message.copy(ttl = message.ttl - 1)
        scope.launch {
            delay(RELAY_DELAY_MS)
            broadcast(relayMsg)
        }
    }

    fun hasSeenMessage(senderId: String, timestamp: Long, type: MessageType): Boolean {
        val key = "${senderId}_${timestamp}_${type}"
        return seenMessages.contains(key)
    }

    fun resetCache() {
        seenMessages.clear()
    }

    companion object {
        private const val TAG = "GossipProtocol"
        private const val MAX_SEEN_CACHE = 500
        private const val RELAY_DELAY_MS = 100L
    }
}