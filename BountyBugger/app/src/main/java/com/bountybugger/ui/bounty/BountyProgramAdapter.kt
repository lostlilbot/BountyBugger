package com.bountybugger.ui.bounty

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bountybugger.R
import com.bountybugger.databinding.ItemBountyProgramBinding
import com.bountybugger.domain.model.BountyProgram

/**
 * Adapter for displaying bounty programs in a RecyclerView
 */
class BountyProgramAdapter(
    private val onProgramClick: (BountyProgram) -> Unit,
    private val onFavoriteClick: (BountyProgram) -> Unit,
    private val onVisitClick: (BountyProgram) -> Unit,
    private val onScanClick: (BountyProgram) -> Unit
) : ListAdapter<BountyProgram, BountyProgramAdapter.ProgramViewHolder>(ProgramDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgramViewHolder {
        val binding = ItemBountyProgramBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProgramViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProgramViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProgramViewHolder(
        private val binding: ItemBountyProgramBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(program: BountyProgram) {
            with(binding) {
                // Platform badge
                textPlatform.text = program.platform.displayName

                // Program name
                textProgramName.text = program.name

                // Description
                textDescription.text = program.description ?: "No description available"

                // Bounty range
                val bountyText = when {
                    program.minBounty != null && program.maxBounty != null -> 
                        "$${program.minBounty} - $${program.maxBounty}"
                    program.minBounty != null -> "From $${program.minBounty}"
                    program.maxBounty != null -> "Up to $${program.maxBounty}"
                    else -> "Contact for bounty"
                }
                textBounty.text = bountyText

                // Types
                textTypes.text = program.bountyType.joinToString(", ") { it.displayName }

                // Industry
                textIndustry.text = program.industry.joinToString(", ") { it.displayName }

                // Badges
                badgeSafeHarbor.visibility = if (program.safeHarbor) View.VISIBLE else View.GONE
                badgePrivate.visibility = if (program.isPrivate) View.VISIBLE else View.GONE

                // Favorite icon
                val favoriteIcon = if (program.isFavorite) {
                    R.drawable.ic_favorite_filled
                } else {
                    R.drawable.ic_favorite_outline
                }
                btnFavorite.setImageResource(favoriteIcon)

                // Click listeners
                root.setOnClickListener { onProgramClick(program) }
                btnFavorite.setOnClickListener { onFavoriteClick(program) }
                btnOpenUrl.setOnClickListener { onVisitClick(program) }
                btnScan.setOnClickListener { onScanClick(program) }
            }
        }
    }

    class ProgramDiffCallback : DiffUtil.ItemCallback<BountyProgram>() {
        override fun areItemsTheSame(oldItem: BountyProgram, newItem: BountyProgram): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BountyProgram, newItem: BountyProgram): Boolean {
            return oldItem == newItem
        }
    }
}
