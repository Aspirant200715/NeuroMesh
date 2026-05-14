package com.neuromesh.crisis.presentation.ui.alert

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.neuromesh.crisis.R
import com.neuromesh.crisis.data.model.CrisisAlert
import com.neuromesh.crisis.data.model.CrisisType
import com.neuromesh.crisis.data.model.SeverityLevel
import com.neuromesh.crisis.databinding.ViewCrisisAlertBinding
import com.neuromesh.crisis.util.visible
import com.neuromesh.crisis.util.gone

class CrisisAlertView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val binding = ViewCrisisAlertBinding.inflate(LayoutInflater.from(context), this, true)

    fun showAlert(alert: CrisisAlert) {
        binding.apply {
            tvAlertTitle.text = alert.title
            tvAlertSummary.text = alert.summary
            tvSeverity.text = alert.severity.name
            tvContributors.text = if (alert.isConsensusAlert) {
                "Confirmed by ${alert.contributingDevices} devices"
            } else {
                "Local detection"
            }

            tvSeverity.setBackgroundColor(getSeverityColor(alert.severity))

            tvAlertIcon.text = getCrisisEmoji(alert.crisisType)

            tvImmediateActions.text = alert.immediateActions
                .mapIndexed { i, action -> "${i + 1}. $action" }
                .joinToString("\n")

            if (alert.evacuationRoutes.isNotEmpty()) {
                tvEvacuationRoutes.text = alert.evacuationRoutes.joinToString("\n") { "• $it" }
                evacuationContainer.visible()
            } else {
                evacuationContainer.gone()
            }

            if (alert.doNotDo.isNotEmpty()) {
                tvDoNotDo.text = alert.doNotDo.joinToString("\n") { "✗ $it" }
                doNotDoContainer.visible()
            } else {
                doNotDoContainer.gone()
            }

            tvGuidance.text = alert.guidanceText

            if (alert.contactNumbers.isNotEmpty()) {
                tvContacts.text = alert.contactNumbers.joinToString("\n") { "${it.name}: ${it.number}" }
                contactsContainer.visible()
            } else {
                contactsContainer.gone()
            }
        }
    }

    private fun getSeverityColor(severity: SeverityLevel): Int = when (severity) {
        SeverityLevel.LOW -> context.getColor(R.color.severity_low)
        SeverityLevel.MODERATE -> context.getColor(R.color.severity_moderate)
        SeverityLevel.HIGH -> context.getColor(R.color.severity_high)
        SeverityLevel.CRITICAL -> context.getColor(R.color.severity_critical)
        SeverityLevel.CATASTROPHIC -> context.getColor(R.color.severity_catastrophic)
    }

    private fun getCrisisEmoji(type: CrisisType): String = when (type) {
        CrisisType.FIRE -> "🔥"
        CrisisType.EARTHQUAKE -> "🌍"
        CrisisType.FLOOD -> "🌊"
        CrisisType.STRUCTURAL_COLLAPSE -> "🏚"
        CrisisType.MEDICAL_EMERGENCY -> "🚑"
        CrisisType.UNKNOWN -> "⚠️"
    }
}
