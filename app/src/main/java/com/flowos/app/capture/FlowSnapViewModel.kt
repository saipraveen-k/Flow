package com.flowos.app.capture

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flowos.app.di.AppContainer
import com.flowos.app.domain.model.AIAnalysisResult
import com.flowos.app.domain.model.CaptureDraft
import com.flowos.app.domain.model.SourceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

sealed interface FlowSnapUiState {
    data object Idle : FlowSnapUiState
    data object Capturing : FlowSnapUiState
    data class Analyzing(val screenshot: Bitmap) : FlowSnapUiState
    data class Success(val screenshot: Bitmap, val insight: FlowSnapIntelligenceEngine.SnapInsight) : FlowSnapUiState
    data class Error(val message: String) : FlowSnapUiState
}

class FlowSnapViewModel(
    application: Application,
    private val container: AppContainer
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<FlowSnapUiState>(FlowSnapUiState.Idle)
    val uiState: StateFlow<FlowSnapUiState> = _uiState.asStateFlow()

    fun onScreenshotCaptured(bitmap: Bitmap) {
        _uiState.value = FlowSnapUiState.Analyzing(bitmap)
        viewModelScope.launch {
            try {
                // 1. Persist bitmap to temporary file for OCR
                val file = File(getApplication<Application>().cacheDir, "snap_${UUID.randomUUID()}.jpg")
                file.outputStream().use { 
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
                }
                val uri = Uri.fromFile(file)
                
                // 2. Perform OCR
                val ocrResult = container.ocrProcessor.extractText(uri)
                if (ocrResult is OCRProcessor.OcrResult.Success) {
                    // 3. Analyze text via FlowSnap engine
                    val insight = FlowSnapIntelligenceEngine.analyze(ocrResult.text)
                    _uiState.value = FlowSnapUiState.Success(bitmap, insight)
                } else {
                    _uiState.value = FlowSnapUiState.Error("FlowOS couldn't read the screen content.")
                }
            } catch (_: Exception) {
                _uiState.value = FlowSnapUiState.Error("Snag during screen analysis.")
            }
        }
    }

    fun saveToFlowSpace(insight: FlowSnapIntelligenceEngine.SnapInsight) {
        viewModelScope.launch {
            container.repository.logActivity(
                "SNAP_SAVED",
                insight.summary,
                insight.category
            )
            // Logic to save to Flow Space...
        }
    }

    fun addToCalendar(insight: FlowSnapIntelligenceEngine.SnapInsight) {
        viewModelScope.launch {
            insight.detectedDate?.let { date ->
                container.actionEngine.scheduleCalendarEvent(insight.summary, date)
            }
        }
    }

    fun setReminder(insight: FlowSnapIntelligenceEngine.SnapInsight) {
        viewModelScope.launch {
            insight.detectedDate?.let { date ->
                container.actionEngine.scheduleReminder(insight.summary, date)
            }
        }
    }
}
