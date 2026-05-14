package com.neuromesh.crisis.presentation.ui.mesh

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.neuromesh.crisis.R
import com.neuromesh.crisis.data.model.ConnectionState
import com.neuromesh.crisis.data.model.MeshPeer
import com.neuromesh.crisis.databinding.ViewMeshStatusBinding

class MeshStatusIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val binding = ViewMeshStatusBinding.inflate(LayoutInflater.from(context), this, true)

    fun updatePeerCount(count: Int) {
        binding.tvPeerCount.text = if (count == 0) {
            context.getString(R.string.mesh_isolated)
        } else {
            context.getString(R.string.mesh_connected_count, count)
        }

        val iconRes = if (count > 0) R.drawable.ic_mesh_connected else R.drawable.ic_mesh_disconnected
        binding.ivMeshIcon.setImageResource(iconRes)

        val indicatorColor = when {
            count == 0 -> context.getColor(R.color.mesh_isolated)
            count == 1 -> context.getColor(R.color.mesh_weak)
            count <= 3 -> context.getColor(R.color.mesh_moderate)
            else -> context.getColor(R.color.mesh_strong)
        }
        binding.meshStrengthIndicator.setBackgroundColor(indicatorColor)
    }

    fun updatePeers(peers: List<MeshPeer>) {
        val connected = peers.count { it.connectionState == ConnectionState.CONNECTED }
        val discovered = peers.count { it.connectionState == ConnectionState.DISCOVERED }
        binding.tvMeshDetail.text = when {
            peers.isEmpty() -> context.getString(R.string.mesh_scanning)
            discovered > 0 -> "Connected: $connected | Nearby: $discovered"
            else -> "Mesh: $connected device${if (connected != 1) "s" else ""}"
        }
    }
}
