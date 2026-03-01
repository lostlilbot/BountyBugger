package com.bountybugger.ui.reports

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bountybugger.databinding.ItemReportBinding
import java.io.File

/**
 * Adapter for displaying reports
 */
class ReportAdapter(
    private val onClick: (File) -> Unit
) : ListAdapter<File, ReportAdapter.ReportViewHolder>(ReportDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReportViewHolder(
        private val binding: ItemReportBinding,
        private val onClick: (File) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(file: File) {
            binding.textName.text = file.name
            binding.textDate.text = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(file.lastModified()))
            binding.textSize.text = "${file.length() / 1024} KB"
            binding.root.setOnClickListener { onClick(file) }
        }
    }

    class ReportDiffCallback : DiffUtil.ItemCallback<File>() {
        override fun areItemsTheSame(oldItem: File, newItem: File) = oldItem.absolutePath == newItem.absolutePath
        override fun areContentsTheSame(oldItem: File, newItem: File) = oldItem == newItem
    }
}
