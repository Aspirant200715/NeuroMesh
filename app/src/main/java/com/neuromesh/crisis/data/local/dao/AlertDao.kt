package com.neuromesh.crisis.data.local.dao

import androidx.room.*
import com.neuromesh.crisis.data.local.entity.AlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alert: AlertEntity)

    @Query("SELECT * FROM alerts WHERE dismissed = 0 ORDER BY timestamp DESC")
    fun observeActiveAlerts(): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts ORDER BY timestamp DESC LIMIT :limit")
    fun observeAll(limit: Int = 20): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE id = :id")
    suspend fun getById(id: String): AlertEntity?

    @Query("UPDATE alerts SET dismissed = 1 WHERE id = :id")
    suspend fun dismiss(id: String)

    @Query("DELETE FROM alerts WHERE expiresAt < :now")
    suspend fun deleteExpired(now: Long)

    @Query("SELECT * FROM alerts WHERE isConsensusAlert = 1 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestConsensusAlert(): AlertEntity?
}
