package com.bountybugger.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for bounty programs
 */
@Dao
interface BountyProgramDao {

    @Query("SELECT * FROM bounty_programs ORDER BY savedAt DESC")
    fun getAllPrograms(): Flow<List<BountyProgramEntity>>

    @Query("SELECT * FROM bounty_programs WHERE isFavorite = 1 ORDER BY savedAt DESC")
    fun getFavoritePrograms(): Flow<List<BountyProgramEntity>>

    @Query("SELECT * FROM bounty_programs WHERE id = :id")
    suspend fun getProgramById(id: String): BountyProgramEntity?

    @Query("SELECT * FROM bounty_programs WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY savedAt DESC")
    fun searchPrograms(query: String): Flow<List<BountyProgramEntity>>

    @Query("SELECT * FROM bounty_programs WHERE platform = :platform ORDER BY savedAt DESC")
    fun getProgramsByPlatform(platform: String): Flow<List<BountyProgramEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgram(program: BountyProgramEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrograms(programs: List<BountyProgramEntity>)

    @Update
    suspend fun updateProgram(program: BountyProgramEntity)

    @Query("UPDATE bounty_programs SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)

    @Delete
    suspend fun deleteProgram(program: BountyProgramEntity)

    @Query("DELETE FROM bounty_programs WHERE isFavorite = 0")
    suspend fun clearNonFavoritePrograms()

    @Query("DELETE FROM bounty_programs WHERE cachedAt < :timestamp AND isFavorite = 0")
    suspend fun clearOldCache(timestamp: Long)

    @Query("SELECT COUNT(*) FROM bounty_programs")
    suspend fun getProgramCount(): Int

    @Query("SELECT COUNT(*) FROM bounty_programs WHERE isFavorite = 1")
    suspend fun getFavoriteCount(): Int
}
