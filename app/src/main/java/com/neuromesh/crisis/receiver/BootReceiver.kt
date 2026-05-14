package com.neuromesh.crisis.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.neuromesh.crisis.util.Logger

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Logger.i(TAG, "Boot completed - NeuroMesh ready")
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
