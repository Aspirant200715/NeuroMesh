package com.neuromesh.crisis.util

object Constants {
    const val SERVICE_ID = "com.neuromesh.crisis.MESH"
    const val HEARTBEAT_INTERVAL_MS = 10_000L
    const val MAX_DETECTION_LATENCY_MS = 5_000L
    const val OBSERVATION_WINDOW_MS = 60_000L
    const val MIN_OBSERVATION_CONFIDENCE = 0.3f
    const val MIN_ALERT_CONFIDENCE = 0.6f
    const val DETECTION_INTERVAL_MS = 3_000L
    const val ALERT_EXPIRY_MS = 30 * 60 * 1000L
    const val DB_RETENTION_MS = 24 * 60 * 60 * 1000L
    const val NOTIFICATION_ID_CRISIS = 1001
    const val NOTIFICATION_ID_MESH = 1002
}
