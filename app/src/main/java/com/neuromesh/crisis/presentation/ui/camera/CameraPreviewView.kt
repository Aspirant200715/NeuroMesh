package com.neuromesh.crisis.presentation.ui.camera

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.camera.view.PreviewView
import com.neuromesh.crisis.R
import com.neuromesh.crisis.databinding.ViewCameraPreviewBinding

class CameraPreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binding = ViewCameraPreviewBinding.inflate(LayoutInflater.from(context), this)

    val previewView: PreviewView get() = binding.cameraPreviewView

    fun setDetecting(isDetecting: Boolean) {
        binding.observationOverlay.setAnalyzing(isDetecting)
        binding.scanPulse.visibility = if (isDetecting) VISIBLE else GONE
    }

    fun setConfidence(confidence: Float) {
        binding.observationOverlay.setConfidence(confidence)
    }
}
