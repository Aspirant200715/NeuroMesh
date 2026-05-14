package com.neuromesh.crisis.presentation.ui.mesh

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.neuromesh.crisis.data.model.MeshPeer
import com.neuromesh.crisis.databinding.ViewPeerListBinding
import com.neuromesh.crisis.presentation.adapter.PeerListAdapter

class PeerListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val binding = ViewPeerListBinding.inflate(LayoutInflater.from(context), this, true)
    private val adapter = PeerListAdapter()

    init {
        binding.recyclerPeers.layoutManager = LinearLayoutManager(context)
        binding.recyclerPeers.adapter = adapter
    }

    fun updatePeers(peers: List<MeshPeer>) {
        adapter.submitList(peers)
    }
}
