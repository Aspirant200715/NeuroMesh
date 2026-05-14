package com.neuromesh.crisis.data.repository

import com.neuromesh.crisis.data.local.dao.ObservationDao
import com.neuromesh.crisis.data.local.entity.ObservationEntity
import com.neuromesh.crisis.data.model.CrisisType
import com.neuromesh.crisis.data.model.Observation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObservationRepository @Inject constructor(
    private val dao: ObservationDao,
    private val json: Json
) {

    fun observeRecent(limit: Int = 50): Flow<List<Observation>> =
        dao.observeRecent(limit).map { entities -> entities.map { it.toDomain(json) } }

    fun observeByType(type: CrisisType): Flow<List<Observation>> =
        dao.observeByType(type).map { entities -> entities.map { it.toDomain(json) } }

    suspend fun save(observation: Observation) {
        dao.insert(observation.toEntity(json))
    }

    suspend fun saveAll(observations: List<Observation>) {
        dao.insertAll(observations.map { it.toEntity(json) })
    }

    suspend fun getRecentSince(since: Long): List<Observation> =
        dao.getObservationsSince(since).map { it.toDomain(json) }

    suspend fun countByCrisisTypeSince(type: CrisisType, since: Long): Int =
        dao.countByTypeSince(type, since)

    suspend fun averageConfidenceSince(type: CrisisType, since: Long): Float =
        dao.avgConfidenceByTypeSince(type, since) ?: 0f

    suspend fun pruneOlderThan(before: Long) {
        dao.deleteOlderThan(before)
    }

    private fun Observation.toEntity(json: Json) = ObservationEntity(
        id = id,
        deviceId = deviceId,
        timestamp = timestamp,
        crisisType = crisisType,
        confidence = confidence,
        rawText = rawText,
        visualFeaturesJson = visualFeatures?.let { json.encodeToString(it) },
        audioFeaturesJson = audioFeatures?.let { json.encodeToString(it) },
        sensorFeaturesJson = sensorFeatures?.let { json.encodeToString(it) },
        locationJson = location?.let { json.encodeToString(it) }
    )

    private fun ObservationEntity.toDomain(json: Json) = Observation(
        id = id,
        deviceId = deviceId,
        timestamp = timestamp,
        crisisType = crisisType,
        confidence = confidence,
        rawText = rawText,
        visualFeatures = visualFeaturesJson?.let {
            json.decodeFromString(it)
        },
        audioFeatures = audioFeaturesJson?.let {
            json.decodeFromString(it)
        },
        sensorFeatures = sensorFeaturesJson?.let {
            json.decodeFromString(it)
        },
        location = locationJson?.let {
            json.decodeFromString(it)
        }
    )
}
