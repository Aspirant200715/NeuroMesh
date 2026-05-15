package com.neuromesh.crisis.util

import android.app.ActivityManager
import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Decides whether this device can realistically host the on-device Gemma model.
 *
 * A 2B-parameter LLM (~1.3 GB on disk) needs the file mmap'd plus KV-cache and
 * activation buffers on top. On phones with less than ~4 GB total RAM the OS
 * low-memory killer terminates the process during model init or first inference
 * — which is the "opens and immediately closes" crash. We detect that up front
 * and run in heuristic-only mode instead of crashing.
 */
@Singleton
class DeviceCapability @Inject constructor(private val context: Context) {

    fun totalRamMb(): Long {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        return info.totalMem / (1024 * 1024)
    }

    /** True only when there is enough headroom to load the LLM safely. */
    fun canHostLlm(): Boolean = totalRamMb() >= MIN_RAM_FOR_LLM_MB

    fun isLowMemory(): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        return info.lowMemory
    }

    companion object {
        // Empirically the smallest total RAM where Gemma 4 E2B q4 loads without
        // being killed, leaving room for camera + mesh + the OS.
        const val MIN_RAM_FOR_LLM_MB = 3800L
    }
}
