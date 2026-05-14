package com.neuromesh.crisis

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp
import com.neuromesh.crisis.util.Logger

@HiltAndroidApp
class NeuroMeshApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Logger.init(this)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val crisisChannel = NotificationChannel(
                CHANNEL_CRISIS_ALERT,
                "Crisis Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent crisis detection alerts"
                enableVibration(true)
                enableLights(true)
            }

            val meshChannel = NotificationChannel(
                CHANNEL_MESH_STATUS,
                "Mesh Network",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mesh network status updates"
            }

            manager.createNotificationChannels(listOf(crisisChannel, meshChannel))
        }
    }

    companion object {
        const val CHANNEL_CRISIS_ALERT = "crisis_alert"
        const val CHANNEL_MESH_STATUS = "mesh_status"
    }
}
