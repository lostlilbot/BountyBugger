package com.bountybugger.ui.tools

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bountybugger.databinding.ItemToolBinding
import com.bountybugger.domain.model.Tool

/**
 * Adapter for displaying tools
 */
class ToolAdapter(
    private val onDownloadClick: (Tool) -> Unit,
    private val onInstallClick: (Tool) -> Unit
) : ListAdapter<Tool, ToolAdapter.ToolViewHolder>(ToolDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolViewHolder {
        val binding = ItemToolBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ToolViewHolder(binding, onDownloadClick, onInstallClick)
    }

    override fun onBindViewHolder(holder: ToolViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ToolViewHolder(
        private val binding: ItemToolBinding,
        private val onDownloadClick: (Tool) -> Unit,
        private val onInstallClick: (Tool) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tool: Tool) {
            binding.textName.text = tool.name
            binding.textDescription.text = tool.description
            binding.textCategory.text = tool.category.displayName
            binding.textStars.text = "★ ${tool.stars}"
            binding.textPlatform.text = tool.platform.displayName

            binding.btnDownload.setOnClickListener { onDownloadClick(tool) }
            binding.btnInstall.setOnClickListener { onInstallClick(tool) }
        }
    }

    class ToolDiffCallback : DiffUtil.ItemCallback<Tool>() {
        override fun areItemsTheSame(oldItem: Tool, newItem: Tool) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Tool, newItem: Tool) = oldItem == newItem
    }
}
