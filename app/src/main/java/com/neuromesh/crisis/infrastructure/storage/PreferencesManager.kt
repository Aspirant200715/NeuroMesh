package com.neuromesh.crisis.infrastructure.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "neuromesh_prefs")

@Singleton
class PreferencesManager @Inject constructor(private val context: Context) {

    private val dataStore = context.dataStore

    val deviceId: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_DEVICE_ID] ?: generateDeviceId().also { id ->
            dataStore.edit { it[KEY_DEVICE_ID] = id }
        }
    }

    val isModelLoaded: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_MODEL_LOADED] ?: false
    }

    val detectionEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_DETECTION_ENABLED] ?: true
    }

    val meshEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_MESH_ENABLED] ?: true
    }

    val minConfidenceThreshold: Flow<Float> = dataStore.data.map { prefs ->
        prefs[KEY_MIN_CONFIDENCE] ?: 0.65f
    }

    suspend fun setDeviceId(id: String) {
        dataStore.edit { it[KEY_DEVICE_ID] = id }
    }

    suspend fun setModelLoaded(loaded: Boolean) {
        dataStore.edit { it[KEY_MODEL_LOADED] = loaded }
    }

    suspend fun setDetectionEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_DETECTION_ENABLED] = enabled }
    }

    suspend fun setMeshEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_MESH_ENABLED] = enabled }
    }

    private fun generateDeviceId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..12).map { chars.random() }.joinToString("")
    }

    companion object {
        private val KEY_DEVICE_ID = stringPreferencesKey("device_id")
        private val KEY_MODEL_LOADED = booleanPreferencesKey("model_loaded")
        private val KEY_DETECTION_ENABLED = booleanPreferencesKey("detection_enabled")
        private val KEY_MESH_ENABLED = booleanPreferencesKey("mesh_enabled")
        private val KEY_MIN_CONFIDENCE = floatPreferencesKey("min_confidence")
    }
}
