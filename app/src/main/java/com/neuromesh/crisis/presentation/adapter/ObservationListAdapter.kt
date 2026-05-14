package com.neuromesh.crisis.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.neuromesh.crisis.data.model.Observation
import com.neuromesh.crisis.databinding.ItemObservationBinding
import com.neuromesh.crisis.util.toPercent
import com.neuromesh.crisis.util.toRelativeTimeString

class ObservationListAdapter : ListAdapter<Observation, ObservationListAdapter.ObservationViewHolder>(ObservationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObservationViewHolder {
        val binding = ItemObservationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ObservationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ObservationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ObservationViewHolder(private val binding: ItemObservationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(obs: Observation) {
            binding.tvCrisisType.text = obs.crisisType.name
            binding.tvConfidence.text = obs.confidence.toPercent()
            binding.tvTimestamp.text = obs.timestamp.toRelativeTimeString()
            binding.tvDeviceId.text = obs.deviceId.take(8)
            binding.tvRawText.text = obs.rawText.take(100)
        }
    }

    class ObservationDiffCallback : DiffUtil.ItemCallback<Observation>() {
        override fun areItemsTheSame(oldItem: Observation, newItem: Observation) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Observation, newItem: Observation) = oldItem == newItem
    }
}
