package com.bountybugger.ui.bounty

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bountybugger.R
import com.bountybugger.data.local.BountyDatabase
import com.bountybugger.data.repository.BountyRepository
import com.bountybugger.databinding.ActivityBountySearchBinding
import com.bountybugger.domain.model.*
import com.bountybugger.ui.web.WebScannerActivity
import kotlinx.coroutines.launch

/**
 * Activity for searching and browsing bug bounty programs
 */
class BountySearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBountySearchBinding
    private lateinit var repository: BountyRepository
    private lateinit var adapter: BountyProgramAdapter

    private var currentFilters = BountySearchFilters()
    private var isFilterVisible = false
    private var showFavoritesOnly = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBountySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRepository()
        setupRecyclerView()
        setupSearchBar()
        setupFilters()
        setupSwipeRefresh()

        loadPrograms()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.bounty_search_title)
    }

    private fun setupRepository() {
        val database = BountyDatabase.getInstance(applicationContext)
        repository = BountyRepository(database.bountyProgramDao())
    }

    private fun setupRecyclerView() {
        adapter = BountyProgramAdapter(
            onProgramClick = { program ->
                // Show program details
                Toast.makeText(this, "Selected: ${program.name}", Toast.LENGTH_SHORT).show()
            },
            onFavoriteClick = { program ->
                lifecycleScope.launch {
                    repository.toggleFavorite(program.id)
                    loadPrograms()
                }
            },
            onVisitClick = { program ->
                // Open program URL in browser
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(program.url))
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Cannot open URL", Toast.LENGTH_SHORT).show()
                }
            },
            onScanClick = { program ->
                // Launch web scanner with program URL
                val intent = Intent(this, WebScannerActivity::class.java).apply {
                    putExtra("target_url", program.url)
                }
                startActivity(intent)
            }
        )

        binding.recyclerPrograms.apply {
            layoutManager = LinearLayoutManager(this@BountySearchActivity)
            adapter = this@BountySearchActivity.adapter
        }
    }

    private fun setupSearchBar() {
        binding.btnSearch.setOnClickListener {
            val query = binding.editSearchQuery.text.toString()
            currentFilters = currentFilters.copy(query = query)
            loadPrograms()
        }
    }

    private fun setupFilters() {
        // Setup reward range spinner
        val rewardRanges = RewardRange.entries.map { it.displayName }
        val rewardAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, rewardRanges)
        rewardAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRewardRange.adapter = rewardAdapter

        // Setup sort options spinner
        val sortOptions = SortOption.entries.map { it.displayName }
        val sortAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSortBy.adapter = sortAdapter

        // Setup type chips
        setupTypeChips()

        // Setup industry chips
        setupIndustryChips()

        // Setup platform chips
        setupPlatformChips()

        // Filter toggle button (in menu)
        binding.btnApplyFilters.setOnClickListener {
            applyFilters()
            toggleFilterVisibility(false)
        }

        binding.btnClearFilters.setOnClickListener {
            clearFilters()
        }

        // Private only checkbox
        binding.checkPrivateOnly.setOnCheckedChangeListener { _, isChecked ->
            currentFilters = currentFilters.copy(onlyPrivate = isChecked)
        }

        // With bounties checkbox
        binding.checkWithBounties.setOnCheckedChangeListener { _, isChecked ->
            currentFilters = currentFilters.copy(onlyWithBounties = isChecked)
        }
    }

    private fun setupTypeChips() {
        val chipGroup = binding.chipGroupTypes
        BountyType.entries.forEach { type ->
            val chip = com.google.android.material.chip.Chip(this).apply {
                text = type.displayName
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    val currentTypes = currentFilters.types.toMutableList()
                    if (isChecked) {
                        currentTypes.add(type)
                    } else {
                        currentTypes.remove(type)
                    }
                    currentFilters = currentFilters.copy(types = currentTypes)
                }
            }
            chipGroup.addView(chip)
        }
    }

    private fun setupIndustryChips() {
        val chipGroup = binding.chipGroupIndustries
        Industry.entries.forEach { industry ->
            val chip = com.google.android.material.chip.Chip(this).apply {
                text = industry.displayName
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    val currentIndustries = currentFilters.industries.toMutableList()
                    if (isChecked) {
                        currentIndustries.add(industry)
                    } else {
                        currentIndustries.remove(industry)
                    }
                    currentFilters = currentFilters.copy(industries = currentIndustries)
                }
            }
            chipGroup.addView(chip)
        }
    }

    private fun setupPlatformChips() {
        val chipGroup = binding.chipGroupPlatforms
        // Only show major platforms
        val majorPlatforms = listOf(
            BountyPlatform.HACKERONE,
            BountyPlatform.BUGCROWD,
            BountyPlatform.INTIGRITI,
            BountyPlatform.YESWEHACK,
            BountyPlatform.IMMUNEFI,
            BountyPlatform.GOOGLE_VRP,
            BountyPlatform.META_BBP,
            BountyPlatform.MICROSOFT_BRP,
            BountyPlatform.APPLE_BRP,
            BountyPlatform.GITHUB_BBP
        )
        majorPlatforms.forEach { platform ->
            val chip = com.google.android.material.chip.Chip(this).apply {
                text = platform.displayName
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    val currentPlatforms = currentFilters.platforms.toMutableList()
                    if (isChecked) {
                        currentPlatforms.add(platform)
                    } else {
                        currentPlatforms.remove(platform)
                    }
                    currentFilters = currentFilters.copy(platforms = currentPlatforms)
                }
            }
            chipGroup.addView(chip)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                repository.refreshPrograms()
                loadPrograms()
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun loadPrograms() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val result = repository.searchPrograms(currentFilters)
                showLoading(false)

                if (result.programs.isEmpty()) {
                    showEmpty(true)
                    adapter.submitList(emptyList())
                } else {
                    showEmpty(false)
                    adapter.submitList(result.programs)
                }
            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(
                    this@BountySearchActivity,
                    getString(R.string.error_search_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun applyFilters() {
        // Get reward range from spinner
        val rewardRangeIndex = binding.spinnerRewardRange.selectedItemPosition
        if (rewardRangeIndex >= 0) {
            currentFilters = currentFilters.copy(rewardRange = RewardRange.entries[rewardRangeIndex])
        }

        // Get sort option from spinner
        val sortIndex = binding.spinnerSortBy.selectedItemPosition
        if (sortIndex >= 0) {
            currentFilters = currentFilters.copy(sortBy = SortOption.entries[sortIndex])
        }

        loadPrograms()
    }

    private fun clearFilters() {
        // Reset spinners
        binding.spinnerRewardRange.setSelection(0)
        binding.spinnerSortBy.setSelection(0)

        // Clear checkboxes
        binding.checkPrivateOnly.isChecked = false
        binding.checkWithBounties.isChecked = false

        // Clear chips
        binding.chipGroupTypes.clearCheck()
        binding.chipGroupIndustries.clearCheck()
        binding.chipGroupPlatforms.clearCheck()

        // Reset filters
        currentFilters = BountySearchFilters()

        loadPrograms()
    }

    private fun toggleFilterVisibility(show: Boolean) {
        isFilterVisible = show
        binding.layoutFilters.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showLoading(show: Boolean) {
        binding.layoutLoading.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            binding.layoutEmpty.visibility = View.GONE
        }
    }

    private fun showEmpty(show: Boolean) {
        binding.layoutEmpty.visibility = if (show) View.VISIBLE else View.GONE
        binding.layoutLoading.visibility = View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bounty_search, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_filter -> {
                toggleFilterVisibility(!isFilterVisible)
                true
            }
            R.id.action_favorites -> {
                showFavoritesOnly = !showFavoritesOnly
                item.title = if (showFavoritesOnly) getString(R.string.favorites) else getString(R.string.filter)
                loadPrograms()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
