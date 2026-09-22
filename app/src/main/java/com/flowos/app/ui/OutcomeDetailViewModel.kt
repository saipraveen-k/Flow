package com.flowos.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flowos.app.data.local.OutcomeEntity
import com.flowos.app.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OutcomeDetailUiState(val outcome: OutcomeEntity? = null, val deleted: Boolean = false)

class OutcomeDetailViewModel(
    application: Application,
    private val container: AppContainer,
    private val outcomeId: String,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(OutcomeDetailUiState())
    val uiState: StateFlow<OutcomeDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            container.repository.observeOutcome(outcomeId).collect { outcome ->
                _uiState.value = _uiState.value.copy(outcome = outcome)
            }
        }
    }

    fun complete() = viewModelScope.launch { container.repository.completeOutcome(outcomeId) }

    fun delete() = viewModelScope.launch {
        container.repository.deleteOutcome(outcomeId)
        _uiState.value = _uiState.value.copy(deleted = true)
    }
}
