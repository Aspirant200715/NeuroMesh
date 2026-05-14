package com.neuromesh.crisis.data.local.dao

import androidx.room.*
import com.neuromesh.crisis.data.local.entity.ObservationEntity
import com.neuromesh.crisis.data.model.CrisisType
import kotlinx.coroutines.flow.Flow

@Dao
interface ObservationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(observation: ObservationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(observations: List<ObservationEntity>)

    @Query("SELECT * FROM observations ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int = 50): Flow<List<ObservationEntity>>

    @Query("SELECT * FROM observations WHERE crisisType = :type ORDER BY timestamp DESC LIMIT :limit")
    fun observeByType(type: CrisisType, limit: Int = 20): Flow<List<ObservationEntity>>

    @Query("SELECT * FROM observations WHERE timestamp > :since ORDER BY timestamp DESC")
    suspend fun getObservationsSince(since: Long): List<ObservationEntity>

    @Query("SELECT * FROM observations WHERE synced = 0")
    suspend fun getUnsynced(): List<ObservationEntity>

    @Query("UPDATE observations SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<String>)

    @Query("DELETE FROM observations WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)

    @Query("SELECT COUNT(*) FROM observations WHERE timestamp > :since AND crisisType = :type")
    suspend fun countByTypeSince(type: CrisisType, since: Long): Int

    @Query("SELECT AVG(confidence) FROM observations WHERE crisisType = :type AND timestamp > :since")
    suspend fun avgConfidenceByTypeSince(type: CrisisType, since: Long): Float?
}
