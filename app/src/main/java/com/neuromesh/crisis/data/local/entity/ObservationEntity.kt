package com.neuromesh.crisis.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.neuromesh.crisis.data.local.Converters
import com.neuromesh.crisis.data.model.CrisisType

@Entity(tableName = "observations")
@TypeConverters(Converters::class)
data class ObservationEntity(
    @PrimaryKey val id: String,
    val deviceId: String,
    val timestamp: Long,
    val crisisType: CrisisType,
    val confidence: Float,
    val rawText: String,
    val visualFeaturesJson: String?,
    val audioFeaturesJson: String?,
    val sensorFeaturesJson: String?,
    val locationJson: String?,
    val synced: Boolean = false
)

@Entity(tableName = "alerts")
@TypeConverters(Converters::class)
data class AlertEntity(
    @PrimaryKey val id: String,
    val assessmentId: String,
    val timestamp: Long,
    val crisisType: CrisisType,
    val severityValue: Int,
    val title: String,
    val summaryJson: String,
    val isConsensusAlert: Boolean,
    val contributingDevices: Int,
    val expiresAt: Long,
    val dismissed: Boolean = false
)
