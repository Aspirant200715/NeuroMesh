package com.neuromesh.crisis.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.neuromesh.crisis.util.Logger

class ConnectivityReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Logger.d(TAG, "Connectivity changed: ${intent.action} (NeuroMesh is offline-first, connectivity change is informational only)")
    }

    companion object {
        private const val TAG = "ConnectivityReceiver"
    }
}
