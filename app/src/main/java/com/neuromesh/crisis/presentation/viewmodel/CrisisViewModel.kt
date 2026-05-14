package com.neuromesh.crisis.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuromesh.crisis.data.local.dao.AlertDao
import com.neuromesh.crisis.data.model.CrisisAlert
import com.neuromesh.crisis.data.model.SituationAssessment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CrisisViewModel @Inject constructor(
    private val alertDao: AlertDao
) : ViewModel() {

    private val _currentAlert = MutableStateFlow<CrisisAlert?>(null)
    val currentAlert: StateFlow<CrisisAlert?> = _currentAlert.asStateFlow()

    private val _currentAssessment = MutableStateFlow<SituationAssessment?>(null)
    val currentAssessment: StateFlow<SituationAssessment?> = _currentAssessment.asStateFlow()

    val showReasoningTrace: StateFlow<Boolean> = MutableStateFlow(false)

    fun setAlert(alert: CrisisAlert, assessment: SituationAssessment?) {
        _currentAlert.value = alert
        _currentAssessment.value = assessment
    }

    fun dismissAlert() {
        val alertId = _currentAlert.value?.id ?: return
        viewModelScope.launch {
            alertDao.dismiss(alertId)
            _currentAlert.value = null
        }
    }
}
