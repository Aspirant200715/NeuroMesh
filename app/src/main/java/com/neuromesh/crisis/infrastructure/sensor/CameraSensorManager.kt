package com.neuromesh.crisis.infrastructure.sensor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.neuromesh.crisis.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class CameraSensorManager @Inject constructor(private val context: Context) {

    private var imageCapture: ImageCapture? = null
    private var latestBitmap: Bitmap? = null
    private val analysisExecutor = Executors.newSingleThreadExecutor()

    fun bindToLifecycle(lifecycleOwner: LifecycleOwner, preview: Preview) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setJpegQuality(75)
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                        processFrame(imageProxy)
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture,
                    imageAnalyzer
                )
                Logger.d(TAG, "Camera bound to lifecycle")
            } catch (e: Exception) {
                Logger.e(TAG, "Camera binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun processFrame(imageProxy: ImageProxy) {
        try {
            val buffer = imageProxy.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            val bitmap = Bitmap.createBitmap(
                imageProxy.width,
                imageProxy.height,
                Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(java.nio.ByteBuffer.wrap(bytes))
            latestBitmap = scaleBitmap(bitmap, MAX_FRAME_SIZE)
        } catch (e: Exception) {
            Logger.w(TAG, "Frame processing error: ${e.message}")
        } finally {
            imageProxy.close()
        }
    }

    suspend fun captureAndDescribe(): String = withContext(Dispatchers.IO) {
        val bitmap = latestBitmap
            ?: return@withContext "Camera feed unavailable"

        describeBitmap(bitmap)
    }

    private fun describeBitmap(bitmap: Bitmap): String {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        var redSum = 0L
        var greenSum = 0L
        var blueSum = 0L
        var brightPixels = 0
        var darkPixels = 0

        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            redSum += r
            greenSum += g
            blueSum += b
            val brightness = (r + g + b) / 3
            if (brightness > 200) brightPixels++
            if (brightness < 50) darkPixels++
        }

        val total = pixels.size.toFloat()
        val avgR = redSum / total
        val avgG = greenSum / total
        val avgB = blueSum / total
        val brightRatio = brightPixels / total
        val darkRatio = darkPixels / total

        val description = StringBuilder()
        description.append("Image ${width}x${height}. ")

        when {
            avgR > 150 && avgR > avgG * 1.5 && avgR > avgB * 1.5 ->
                description.append("Dominant red/orange tones - potential fire or heat. ")
            avgB > 150 && avgB > avgR * 1.3 ->
                description.append("Dominant blue tones - possible water/flood. ")
            avgR > 100 && avgG > 100 && avgB < 80 ->
                description.append("Yellow/amber tones - possible smoke, fire, or dust. ")
            avgR < 80 && avgG < 80 && avgB < 80 ->
                description.append("Dark scene - low visibility. ")
        }

        if (brightRatio > 0.3f) description.append("High brightness areas (${(brightRatio * 100).toInt()}%). ")
        if (darkRatio > 0.5f) description.append("Scene mostly dark/obscured. ")

        return description.toString().trim()
    }

    private fun scaleBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val ratio = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height)
        if (ratio >= 1f) return bitmap
        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    fun release() {
        analysisExecutor.shutdown()
        latestBitmap?.recycle()
        latestBitmap = null
    }

    companion object {
        private const val TAG = "CameraSensorManager"
        private const val MAX_FRAME_SIZE = 224
    }
}
