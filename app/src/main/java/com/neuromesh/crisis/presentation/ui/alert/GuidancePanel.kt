package com.neuromesh.crisis.presentation.ui.alert

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.neuromesh.crisis.data.model.CrisisAlert
import com.neuromesh.crisis.databinding.ViewCrisisAlertBinding

class GuidancePanel @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    fun showGuidance(alert: CrisisAlert) {
        removeAllViews()
        val alertView = CrisisAlertView(context)
        alertView.showAlert(alert)
        addView(alertView)
    }
}
