package com.neuromesh.crisis.infrastructure.ml

import android.content.Context
import com.neuromesh.crisis.util.Logger
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelLoader @Inject constructor(private val context: Context) {

    private var cachedModelPath: String? = null

    fun getModelPath(): String? {
        cachedModelPath?.let { if (File(it).exists()) return it }

        val modelFile = File(context.filesDir, MODEL_FILENAME)
        if (modelFile.exists()) {
            cachedModelPath = modelFile.absolutePath
            Logger.d(TAG, "Model found at ${modelFile.absolutePath} (${modelFile.length() / 1024 / 1024}MB)")
            return cachedModelPath
        }

        return copyFromAssets(modelFile)
    }

    private fun copyFromAssets(dest: File): String? {
        return try {
            context.assets.open(MODEL_FILENAME).use { input ->
                FileOutputStream(dest).use { output ->
                    val buffer = ByteArray(4 * 1024 * 1024)
                    var read: Int
                    var totalBytes = 0L
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        totalBytes += read
                    }
                    output.flush()
                    Logger.d(TAG, "Model copied from assets: ${totalBytes / 1024 / 1024}MB")
                }
            }
            cachedModelPath = dest.absolutePath
            dest.absolutePath
        } catch (e: Exception) {
            Logger.e(TAG, "Model not found in assets: ${e.message}")
            null
        }
    }

    fun isModelAvailable(): Boolean = getModelPath() != null

    fun getModelSizeMb(): Long {
        val path = getModelPath() ?: return 0L
        return File(path).length() / 1024 / 1024
    }

    companion object {
        private const val TAG = "ModelLoader"
        const val MODEL_FILENAME = "gemma4_e2b_q4.tflite"
    }
}