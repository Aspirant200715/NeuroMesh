package com.neuromesh.crisis.presentation.ui.alert

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.neuromesh.crisis.data.model.ReasoningTrace
import com.neuromesh.crisis.databinding.ViewReasoningTraceBinding

class ReasoningTraceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val binding = ViewReasoningTraceBinding.inflate(LayoutInflater.from(context), this, true)

    fun showTrace(trace: ReasoningTrace) {
        binding.apply {
            tvAgentType.text = "Agent: ${trace.agentType.name}"
            tvConclusion.text = trace.conclusion
            tvConfidence.text = "Confidence: ${(trace.confidence * 100).toInt()}%"

            val stepsText = trace.steps.joinToString("\n\n") { step ->
                "Step ${step.stepNumber}: ${step.description}\nEvidence: ${step.evidence}"
            }
            tvSteps.text = stepsText.ifEmpty { trace.reasoning }
        }
    }
}
