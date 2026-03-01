package com.bountybugger.ui.network

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bountybugger.R
import com.bountybugger.databinding.ItemPortResultBinding
import com.bountybugger.domain.model.PortResult
import com.bountybugger.domain.model.PortState

/**
 * Adapter for displaying port scan results
 */
class PortResultAdapter : ListAdapter<PortResult, PortResultAdapter.PortViewHolder>(PortDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PortViewHolder {
        val binding = ItemPortResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PortViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PortViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PortViewHolder(private val binding: ItemPortResultBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(portResult: PortResult) {
            binding.textPortNumber.text = "Port ${portResult.port}"
            binding.textProtocol.text = portResult.protocol.uppercase()
            binding.textService.text = portResult.service ?: "Unknown"
            binding.textState.text = portResult.state.displayName

            val color = when (portResult.state) {
                PortState.OPEN -> R.color.port_open
                PortState.CLOSED -> R.color.port_closed
                PortState.FILTERED -> R.color.port_filtered
                else -> R.color.text_secondary
            }
            binding.textState.setTextColor(ContextCompat.getColor(binding.root.context, color))

            if (portResult.serviceVersion != null) {
                binding.textVersion.text = portResult.serviceVersion
                binding.textVersion.visibility = android.view.View.VISIBLE
            } else {
                binding.textVersion.visibility = android.view.View.GONE
            }
        }
    }

    class PortDiffCallback : DiffUtil.ItemCallback<PortResult>() {
        override fun areItemsTheSame(oldItem: PortResult, newItem: PortResult): Boolean {
            return oldItem.port == newItem.port
        }

        override fun areContentsTheSame(oldItem: PortResult, newItem: PortResult): Boolean {
            return oldItem == newItem
        }
    }
}
