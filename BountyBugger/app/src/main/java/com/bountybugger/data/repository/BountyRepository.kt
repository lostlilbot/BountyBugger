package com.bountybugger.data.repository

import com.bountybugger.data.local.BountyProgramDao
import com.bountybugger.data.local.BountyProgramEntity
import com.bountybugger.data.remote.BountyApiService
import com.bountybugger.data.remote.SampleBountyData
import com.bountybugger.domain.model.*
import com.bountybugger.domain.model.BountySearchFilters
import com.bountybugger.domain.model.BountySearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository for managing bounty programs from both local database and remote sources.
 */
class BountyRepository(
    private val bountyProgramDao: BountyProgramDao
) {
    private val apiService = BountyApiService.getInstance()
    private var cachedPrograms: List<BountyProgram>? = null
    private var lastFetchTime: Long = 0
    private val cacheValidityMs: Long = 15 * 60 * 1000 // 15 minutes cache

    /**
     * Search for bounty programs based on filters
     */
    suspend fun searchPrograms(filters: BountySearchFilters): BountySearchResult = withContext(Dispatchers.IO) {
        // Get programs from API (with caching)
        val allPrograms = getAllPrograms()
        
        // Apply filters
        val filtered = filterProgramsLocally(allPrograms, filters)
        
        // Get favorite IDs from local DB
        val favoriteIds = getFavoriteIds()
        
        // Mark favorites
        val programsWithFavorites = filtered.map { program ->
            program.copy(isFavorite = program.id in favoriteIds)
        }

        // Paginate
        val pageSize = 20
        val page = filters.query.hashCode().let { kotlin.math.abs(it) % 100 } // Simple page calculation
        val startIndex = page * pageSize
        val endIndex = minOf(startIndex + pageSize, programsWithFavorites.size)
        
        val pagedPrograms = if (startIndex < programsWithFavorites.size) {
            programsWithFavorites.subList(startIndex, endIndex)
        } else {
            emptyList()
        }

        BountySearchResult(
            programs = pagedPrograms,
            totalCount = filtered.size,
            page = page,
            pageSize = pageSize,
            hasMore = endIndex < filtered.size
        )
    }

    /**
     * Get all programs without filtering - fetches from real API
     */
    suspend fun getAllPrograms(forceRefresh: Boolean = false): List<BountyProgram> = withContext(Dispatchers.IO) {
        val currentTime = System.currentTimeMillis()
        
        // Check if cache is valid
        if (!forceRefresh && cachedPrograms != null && (currentTime - lastFetchTime) < cacheValidityMs) {
            return@withContext cachedPrograms!!
        }
        
        // Fetch from real API
        try {
            val programs = apiService.fetchAllPrograms()
            cachedPrograms = programs
            lastFetchTime = currentTime
            programs
        } catch (e: Exception) {
            // Fallback to cached if available
            cachedPrograms ?: emptyList()
        }
    }

    /**
     * Get favorite programs from local database
     */
    fun getFavorites(): Flow<List<BountyProgram>> {
        return bountyProgramDao.getFavoritePrograms().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Toggle favorite status for a program
     */
    suspend fun toggleFavorite(programId: String) = withContext(Dispatchers.IO) {
        val existing = bountyProgramDao.getProgramById(programId)
        if (existing != null) {
            bountyProgramDao.updateFavoriteStatus(programId, !existing.isFavorite)
        } else {
            // Program not in local DB, get from sample data and add
            cachedPrograms?.find { it.id == programId }?.let { program ->
                val entity = BountyProgramEntity.fromDomainModel(program.copy(isFavorite = true))
                bountyProgramDao.insertProgram(entity)
            }
        }
    }

    /**
     * Save a program to favorites
     */
    suspend fun saveProgram(program: BountyProgram) = withContext(Dispatchers.IO) {
        val entity = BountyProgramEntity.fromDomainModel(program.copy(
            isFavorite = true,
            savedAt = System.currentTimeMillis()
        ))
        bountyProgramDao.insertProgram(entity)
    }

    /**
     * Get program by ID
     */
    suspend fun getProgramById(id: String): BountyProgram? = withContext(Dispatchers.IO) {
        // First check local DB
        val localProgram = bountyProgramDao.getProgramById(id)?.toDomainModel()
        if (localProgram != null) return@withContext localProgram

        // Then check cached programs
        cachedPrograms?.find { it.id == id }
    }

    /**
     * Refresh programs from remote source
     */
    suspend fun refreshPrograms() = withContext(Dispatchers.IO) {
        // Clear non-favorite cached programs
        bountyProgramDao.clearNonFavoritePrograms()
        
        // Force refresh from API
        getAllPrograms(forceRefresh = true)
    }

    /**
     * Get all favorite program IDs
     */
    private suspend fun getFavoriteIds(): Set<String> = withContext(Dispatchers.IO) {
        val count = bountyProgramDao.getProgramCount()
        if (count == 0) return@withContext emptySet()
        
        // Get all programs and filter favorites
        val allEntities = mutableListOf<BountyProgramEntity>()
        bountyProgramDao.getAllPrograms().collect { entities ->
            allEntities.clear()
            allEntities.addAll(entities)
        }
        
        allEntities.filter { it.isFavorite }.map { it.id }.toSet()
    }

    /**
     * Get programs by platform
     */
    fun getProgramsByPlatform(platform: String): Flow<List<BountyProgram>> {
        return bountyProgramDao.getProgramsByPlatform(platform).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Search programs locally
     */
    fun searchLocalPrograms(query: String): Flow<List<BountyProgram>> {
        return bountyProgramDao.searchPrograms(query).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Clear old cache
     */
    suspend fun clearOldCache(maxAgeMillis: Long = 7 * 24 * 60 * 60 * 1000L) = withContext(Dispatchers.IO) {
        val cutoff = System.currentTimeMillis() - maxAgeMillis
        bountyProgramDao.clearOldCache(cutoff)
    }

    /**
     * Filter programs locally based on search filters
     */
    private fun filterProgramsLocally(programs: List<BountyProgram>, filters: BountySearchFilters): List<BountyProgram> {
        var filtered = programs

        // Text search
        if (filters.query.isNotBlank()) {
            val query = filters.query.lowercase()
            filtered = filtered.filter {
                it.name.lowercase().contains(query) ||
                it.description?.lowercase()?.contains(query) == true ||
                it.platform.displayName.lowercase().contains(query)
            }
        }

        // Filter by types
        if (filters.types.isNotEmpty()) {
            filtered = filtered.filter { program ->
                program.bountyType.any { it in filters.types }
            }
        }

        // Filter by industries
        if (filters.industries.isNotEmpty()) {
            filtered = filtered.filter { program ->
                program.industry.any { it in filters.industries }
            }
        }

        // Filter by platforms
        if (filters.platforms.isNotEmpty()) {
            filtered = filtered.filter { it.platform in filters.platforms }
        }

        // Filter by reward range
        if (filters.rewardRange != RewardRange.ANY) {
            filtered = filtered.filter { program ->
                val min = program.minBounty ?: 0
                val max = program.maxBounty ?: 0
                min in filters.rewardRange.min..filters.rewardRange.max ||
                max in filters.rewardRange.min..filters.rewardRange.max ||
                (min <= filters.rewardRange.max && max >= filters.rewardRange.min)
            }
        }

        // Filter private only
        if (filters.onlyPrivate) {
            filtered = filtered.filter { it.isPrivate }
        }

        // Filter with bounties only
        if (filters.onlyWithBounties) {
            filtered = filtered.filter { it.minBounty != null && it.minBounty > 0 }
        }

        // Sort
        filtered = when (filters.sortBy) {
            SortOption.NEWEST -> filtered.sortedByDescending { it.publishedAt ?: "" }
            SortOption.OLDEST -> filtered.sortedBy { it.publishedAt ?: "" }
            SortOption.HIGHEST_REWARD -> filtered.sortedByDescending { it.maxBounty ?: 0 }
            SortOption.LOWEST_REWARD -> filtered.sortedBy { it.minBounty ?: 0 }
            SortOption.MOST_SCOPES -> filtered.sortedByDescending { it.scopes.size }
            SortOption.ENDING_SOON -> filtered.sortedBy { it.lastUpdated ?: "" }
        }

        return filtered
    }
}
