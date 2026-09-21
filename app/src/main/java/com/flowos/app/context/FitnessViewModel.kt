package com.flowos.app.context

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FitnessViewModel(
    private val fitnessRepository: FitnessRepository,
    val permissionManager: FitnessPermissionManager
) : ViewModel() {

    val metrics: StateFlow<FitnessMetrics> = fitnessRepository.fitnessMetrics

    fun syncData() {
        viewModelScope.launch {
            fitnessRepository.refreshMetrics()
        }
    }
}
