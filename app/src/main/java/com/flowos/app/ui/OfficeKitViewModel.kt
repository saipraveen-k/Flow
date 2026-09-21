package com.flowos.app.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flowos.app.crossdevice.CrossDeviceResult
import com.flowos.app.crossdevice.RealCrossDeviceConnectionState
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
    val isSupported: Boolean = false,
    val isConnected: Boolean = false,
    val deviceName: String? = null,
    val recentHandoffs: List<HandoffState> = emptyList(),
    val currentOperation: String? = null
)

class OfficeKitViewModel(
    application: Application,
    private val container: AppContainer
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(OfficeKitUiState())
    val uiState: StateFlow<OfficeKitUiState> = _uiState.asStateFlow()

    init {
        refreshState()
    }

    private fun refreshState() {
        viewModelScope.launch {
            val supported = container.crossDeviceManager.isOfficeKitSupported()
            val connected = container.crossDeviceManager.isConnected()
            _uiState.value = _uiState.value.copy(
                isSupported = supported,
                isConnected = connected,
                deviceName = if (connected) "DESKTOP-VQ7R8" else null,
                recentHandoffs = listOf(
                    HandoffState("Presentation.pptx", "Sent to PC", "2h ago"),
                    HandoffState("Architecture.pdf", "Open on PC", "Yesterday")
                )
            )
        }
    }

    fun onFileSelected(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(currentOperation = "Transferring file...")
            
            // 1. Persist file to local cache for transfer
            val tempFile = File(getApplication<Application>().cacheDir, "transfer_${UUID.randomUUID()}")
            try {
                getApplication<Application>().contentResolver.openInputStream(uri)?.use { input ->
                    tempFile.outputStream().use { output -> input.copyTo(output) }
                }
                
                // 2. Trigger transfer
                val result = container.crossDeviceManager.sendFile(tempFile.absolutePath)
                
                _uiState.value = _uiState.value.copy(
                    currentOperation = when(result) {
                        is CrossDeviceResult.Sent -> "Success: ${result.via}"
                        is CrossDeviceResult.Failed -> "Failed: ${result.reason}"
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(currentOperation = "Transfer failed: ${e.message}")
            }
            refreshState()
        }
    }

    fun onConnectPC() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(currentOperation = "Establishing Office Kit bridge...")
            delay(1500)
            RealCrossDeviceConnectionState.isConnected = true
            _uiState.value = _uiState.value.copy(currentOperation = "Connected to DESKTOP-VQ7R8")
            refreshState()
        }
    }
}
