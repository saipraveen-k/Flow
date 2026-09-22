package com.flowos.app.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flowos.app.crossdevice.CrossDeviceResult
import com.flowos.app.crossdevice.RealCrossDeviceConnectionState
import com.flowos.app.crossdevice.LocalFlowBridgeClient
import com.flowos.app.di.AppContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

data class HandoffState(
    val title: String,
    val status: String,
    val timestamp: String
)

data class OfficeKitUiState(
    val isConnected: Boolean = false,
    val host: String = "192.168.1.102",
    val token: String = "",
    val selectedFileName: String? = null,
    val selectedFileSize: Long? = null,
    val recentHandoffs: List<HandoffState> = emptyList(),
    val currentOperation: String? = null
)

class OfficeKitViewModel(
    application: Application,
    private val container: AppContainer
) : AndroidViewModel(application) {

    private val bridgeClient = LocalFlowBridgeClient()

    private val _uiState = MutableStateFlow(OfficeKitUiState())
    val uiState: StateFlow<OfficeKitUiState> = _uiState.asStateFlow()

    init {
        refreshState()
    }

    private fun refreshState() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isConnected = RealCrossDeviceConnectionState.isConnected,
                recentHandoffs = emptyList() // Start empty in production
            )
        }
    }

    fun onFileSelected(uri: Uri) {
        viewModelScope.launch {
            try {
                val resolver = getApplication<Application>().contentResolver
                val name = resolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME, android.provider.OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                    cursor.moveToFirst(); cursor.getString(0) to cursor.getLong(1)
                } ?: ("selected-file" to -1L)
                _uiState.value = _uiState.value.copy(selectedFileName = name.first, selectedFileSize = name.second, currentOperation = "File selected. Tap SEND TO PC.")
                selectedUri = uri
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(currentOperation = "Couldn't read selected file.")
            }
        }
    }

    private var selectedUri: Uri? = null
    fun updateHost(value: String) { _uiState.value = _uiState.value.copy(host = value) }
    fun updateToken(value: String) { _uiState.value = _uiState.value.copy(token = value) }

    fun sendSelectedFile() {
        val uri = selectedUri ?: return
        viewModelScope.launch {
            val state = _uiState.value
            val size = state.selectedFileSize ?: -1L
            if (size < 0) { _uiState.value = state.copy(currentOperation = "File size is unavailable; choose another file."); return@launch }
            _uiState.value = state.copy(currentOperation = "Transferring to local PC...")
            val result = bridgeClient.sendFile(getApplication<Application>().contentResolver, uri, state.selectedFileName ?: "file", size, state.host, state.token)
            val message = when (result) { is CrossDeviceResult.Sent -> result.via; is CrossDeviceResult.Failed -> "Failed: ${result.reason}" }
            _uiState.value = _uiState.value.copy(currentOperation = message, isConnected = result is CrossDeviceResult.Sent)
            RealCrossDeviceConnectionState.isConnected = result is CrossDeviceResult.Sent
        }
    }

    fun onConnectPC() {
        viewModelScope.launch {
            val state = _uiState.value.copy(currentOperation = "Checking local PC connection...")
            _uiState.value = state
            val result = bridgeClient.connect(state.host, state.token)
            val connected = result is CrossDeviceResult.Sent
            _uiState.value = _uiState.value.copy(isConnected = connected, currentOperation = if (connected) result.via else "Failed: ${(result as CrossDeviceResult.Failed).reason}")
            RealCrossDeviceConnectionState.isConnected = connected
        }
    }
}
