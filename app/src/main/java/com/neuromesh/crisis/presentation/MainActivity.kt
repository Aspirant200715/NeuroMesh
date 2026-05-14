package com.neuromesh.crisis.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import com.neuromesh.crisis.R
import com.neuromesh.crisis.databinding.ActivityMainBinding
import com.neuromesh.crisis.infrastructure.sensor.CameraSensorManager
import com.neuromesh.crisis.presentation.ui.alert.CrisisAlertView
import com.neuromesh.crisis.presentation.ui.mesh.MeshStatusIndicator
import com.neuromesh.crisis.presentation.viewmodel.MainViewModel
import com.neuromesh.crisis.presentation.viewmodel.UiState
import com.neuromesh.crisis.util.collectFlow
import com.neuromesh.crisis.util.gone
import com.neuromesh.crisis.util.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var cameraSensorManager: CameraSensorManager

    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.NEARBY_WIFI_DEVICES
    )

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] == true
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] == true
        if (cameraGranted) {
            startCameraPreview()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        checkAndRequestPermissions()
        observeViewModel()
        setupUI()
    }

    private fun setupUI() {
        binding.btnDismissAlert.setOnClickListener {
            viewModel.dismissAlert()
        }
        binding.btnRetry.setOnClickListener {
            viewModel.retryInitialization()
        }
    }

    private fun checkAndRequestPermissions() {
        val notGranted = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isEmpty()) {
            startCameraPreview()
        } else {
            permissionLauncher.launch(notGranted.toTypedArray())
        }
    }

    private fun startCameraPreview() {
        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
        cameraSensorManager.bindToLifecycle(this, preview)
    }

    private fun observeViewModel() {
        collectFlow(viewModel.uiState) { state ->
            updateUiForState(state)
        }

        collectFlow(viewModel.activeAlert, Lifecycle.State.STARTED) { alert ->
            if (alert != null) {
                binding.alertView.showAlert(alert)
                binding.alertContainer.visible()
            } else {
                binding.alertContainer.gone()
            }
        }

        collectFlow(viewModel.connectedPeerCount) { count ->
            binding.meshIndicator.updatePeerCount(count)
        }

        collectFlow(viewModel.peers) { peers ->
            binding.meshIndicator.updatePeers(peers)
        }

        collectFlow(viewModel.latestAssessment) { assessment ->
            assessment?.let {
                binding.statusText.text = "${it.crisisType}: ${(it.confidence * 100).toInt()}%"
            }
        }
    }

    private fun updateUiForState(state: UiState) {
        when (state) {
            is UiState.Initializing -> {
                binding.loadingContainer.visible()
                binding.loadingText.text = getString(R.string.loading_model)
                binding.statusIndicator.gone()
                binding.btnRetry.gone()
            }
            is UiState.ModelNotFound -> {
                binding.loadingContainer.visible()
                binding.loadingText.text = getString(R.string.model_not_found)
                binding.btnRetry.visible()
                binding.statusIndicator.gone()
            }
            is UiState.Ready, is UiState.Monitoring -> {
                binding.loadingContainer.gone()
                binding.statusIndicator.visible()
                binding.statusText.text = getString(R.string.status_monitoring)
                binding.alertContainer.gone()
            }
            is UiState.Detecting -> {
                binding.loadingContainer.gone()
                binding.statusIndicator.visible()
                binding.statusText.text = getString(R.string.status_analyzing)
            }
            is UiState.CrisisDetected -> {
                binding.loadingContainer.gone()
                binding.statusIndicator.visible()
            }
            is UiState.Error -> {
                binding.loadingText.text = state.message
                binding.loadingContainer.visible()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSensorManager.release()
    }
}
