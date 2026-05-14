package com.neuromesh.crisis.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.neuromesh.crisis.data.local.dao.AlertDao
import com.neuromesh.crisis.data.local.dao.ObservationDao
import com.neuromesh.crisis.data.local.entity.AlertEntity
import com.neuromesh.crisis.data.local.entity.ObservationEntity

@Database(
    entities = [ObservationEntity::class, AlertEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NeuroMeshDatabase : RoomDatabase() {
    abstract fun observationDao(): ObservationDao
    abstract fun alertDao(): AlertDao
}
