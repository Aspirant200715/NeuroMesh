package com.neuromesh.crisis.infrastructure.storage

import android.content.Context
import com.neuromesh.crisis.util.Logger
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileStorageManager @Inject constructor(private val context: Context) {

    val modelDir: File get() = context.filesDir
    val cacheDir: File get() = context.cacheDir
    val logsDir: File get() = File(context.filesDir, "logs").also { it.mkdirs() }

    fun getModelFile(filename: String): File = File(modelDir, filename)

    fun modelExists(filename: String): Boolean = getModelFile(filename).exists()

    fun getModelSizeMb(filename: String): Long {
        val file = getModelFile(filename)
        return if (file.exists()) file.length() / 1024 / 1024 else 0L
    }

    fun getAvailableStorageMb(): Long {
        return context.filesDir.freeSpace / 1024 / 1024
    }

    fun writeLogs(content: String, filename: String) {
        try {
            File(logsDir, filename).appendText(content + "\n")
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to write log", e)
        }
    }

    fun clearCache() {
        cacheDir.listFiles()?.forEach { it.delete() }
    }

    companion object {
        private const val TAG = "FileStorageManager"
    }
}
