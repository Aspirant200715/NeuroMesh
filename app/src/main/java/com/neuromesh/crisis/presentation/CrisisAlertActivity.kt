package com.neuromesh.crisis.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.neuromesh.crisis.R
import com.neuromesh.crisis.data.model.CrisisAlert
import com.neuromesh.crisis.data.model.CrisisType
import com.neuromesh.crisis.data.model.SeverityLevel
import com.neuromesh.crisis.databinding.ActivityCrisisAlertBinding
import com.neuromesh.crisis.presentation.viewmodel.CrisisViewModel
import com.neuromesh.crisis.util.collectFlow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json
import javax.inject.Inject

@AndroidEntryPoint
class CrisisAlertActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrisisAlertBinding
    private val viewModel: CrisisViewModel by viewModels()

    @Inject
    lateinit var json: Json

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrisisAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        val alertJson = intent.getStringExtra(EXTRA_ALERT_JSON)
        if (alertJson != null) {
            val alert = json.decodeFromString<CrisisAlert>(alertJson)
            renderAlert(alert)
        } else {
            finish()
            return
        }

        setupListeners()
        observeViewModel()
    }

    private fun renderAlert(alert: CrisisAlert) {
        binding.tvCrisisTitle.text = alert.title
        binding.tvCrisisMeta.text = if (alert.isConsensusAlert) {
            "Confirmed by ${alert.contributingDevices} mesh devices"
        } else {
            "Local detection"
        }
        binding.tvSeverityPill.text = alert.severity.name
        binding.tvSeverityPill.setBackgroundColor(getSeverityColor(alert.severity))
        binding.severityBar.setBackgroundColor(getSeverityColor(alert.severity))
        binding.tvCrisisEmoji.text = getCrisisEmoji(alert.crisisType)

        binding.crisisAlertView.showAlert(alert)
        viewModel.setAlert(alert, null)
    }

    private fun setupListeners() {
        binding.btnAcknowledge.setOnClickListener {
            viewModel.dismissAlert()
            finish()
        }
        binding.btnEmergencyCall.setOnClickListener {
            viewModel.currentAlert.value?.contactNumbers?.firstOrNull()?.let { contact ->
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${contact.number}")
                }
                startActivity(intent)
            }
        }
    }

    private fun observeViewModel() {
        collectFlow(viewModel.currentAssessment) { assessment ->
            assessment?.reasoningTrace?.let {
                binding.reasoningTraceView.showTrace(it)
            }
        }
    }

    private fun getSeverityColor(severity: SeverityLevel): Int = when (severity) {
        SeverityLevel.LOW -> getColor(R.color.severity_low)
        SeverityLevel.MODERATE -> getColor(R.color.severity_moderate)
        SeverityLevel.HIGH -> getColor(R.color.severity_high)
        SeverityLevel.CRITICAL -> getColor(R.color.severity_critical)
        SeverityLevel.CATASTROPHIC -> getColor(R.color.severity_catastrophic)
    }

    private fun getCrisisEmoji(type: CrisisType): String = when (type) {
        CrisisType.FIRE -> "🔥"
        CrisisType.EARTHQUAKE -> "🌍"
        CrisisType.FLOOD -> "🌊"
        CrisisType.STRUCTURAL_COLLAPSE -> "🏚"
        CrisisType.MEDICAL_EMERGENCY -> "🚑"
        CrisisType.UNKNOWN -> "⚠️"
    }

    companion object {
        const val EXTRA_ALERT_JSON = "extra_alert_json"

        fun newIntent(context: android.content.Context, alertJson: String): Intent {
            return Intent(context, CrisisAlertActivity::class.java).apply {
                putExtra(EXTRA_ALERT_JSON, alertJson)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        }
    }
}
