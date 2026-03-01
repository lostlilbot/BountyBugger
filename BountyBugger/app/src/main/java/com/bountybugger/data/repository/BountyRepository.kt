package com.bountybugger.data.repository

import com.bountybugger.data.local.BountyProgramDao
import com.bountybugger.data.local.BountyProgramEntity
import com.bountybugger.data.remote.SampleBountyData
import com.bountybugger.domain.model.BountyProgram
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
    private var cachedPrograms: List<BountyProgram>? = null

    /**
     * Search for bounty programs based on filters
     */
    suspend fun searchPrograms(filters: BountySearchFilters): BountySearchResult = withContext(Dispatchers.IO) {
        // Get programs from sample data
        val allPrograms = getAllPrograms()
        
        // Apply filters
        val filtered = SampleBountyData.filterPrograms(allPrograms, filters)
        
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
     * Get all programs without filtering
     */
    suspend fun getAllPrograms(): List<BountyProgram> = withContext(Dispatchers.IO) {
        cachedPrograms ?: SampleBountyData.getSamplePrograms().also {
            cachedPrograms = it
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
        
        // Reset cache
        cachedPrograms = null
        
        // Re-fetch
        getAllPrograms()
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
}
