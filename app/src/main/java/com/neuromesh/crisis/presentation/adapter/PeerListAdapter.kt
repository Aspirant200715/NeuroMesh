package com.neuromesh.crisis.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.neuromesh.crisis.R
import com.neuromesh.crisis.data.model.ConnectionState
import com.neuromesh.crisis.data.model.MeshPeer
import com.neuromesh.crisis.databinding.ItemPeerBinding

class PeerListAdapter : ListAdapter<MeshPeer, PeerListAdapter.PeerViewHolder>(PeerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeerViewHolder {
        val binding = ItemPeerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PeerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PeerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PeerViewHolder(private val binding: ItemPeerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(peer: MeshPeer) {
            binding.tvPeerName.text = peer.displayName.take(12)
            binding.tvPeerId.text = peer.deviceId.take(8)
            binding.tvConnectionState.text = peer.connectionState.name

            val stateColor = when (peer.connectionState) {
                ConnectionState.CONNECTED -> binding.root.context.getColor(R.color.state_connected)
                ConnectionState.CONNECTING -> binding.root.context.getColor(R.color.state_connecting)
                ConnectionState.DISCONNECTED, ConnectionState.LOST ->
                    binding.root.context.getColor(R.color.state_disconnected)
                ConnectionState.DISCOVERED -> binding.root.context.getColor(R.color.state_discovered)
            }
            binding.connectionIndicator.setBackgroundColor(stateColor)

            binding.tvModelReady.text = if (peer.capabilities.modelLoaded) "AI Ready" else "No Model"
        }
    }

    class PeerDiffCallback : DiffUtil.ItemCallback<MeshPeer>() {
        override fun areItemsTheSame(oldItem: MeshPeer, newItem: MeshPeer) =
            oldItem.deviceId == newItem.deviceId

        override fun areContentsTheSame(oldItem: MeshPeer, newItem: MeshPeer) = oldItem == newItem
    }
}
