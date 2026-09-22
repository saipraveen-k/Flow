package com.flowos.app.capture

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flowos.app.di.AppContainer
import com.flowos.app.domain.model.AIAnalysisResult
import com.flowos.app.domain.model.CaptureDraft
import com.flowos.app.domain.model.IntentType
import com.flowos.app.domain.model.Priority
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

    /**
     * Analyses an image selected through the system picker. This is the
     * reliable in-app Flow Snap path: arbitrary global screenshots cannot be
     * captured by a normal Android app without an explicit system grant.
     */
    fun analyzeImage(uri: Uri) {
        _uiState.value = FlowSnapUiState.Capturing
        viewModelScope.launch {
            try {
                val bitmap = getApplication<Application>().contentResolver
                    .openInputStream(uri)
                    ?.use(BitmapFactory::decodeStream)
                    ?: run {
                        _uiState.value = FlowSnapUiState.Error("The selected image could not be opened.")
                        return@launch
                    }
                _uiState.value = FlowSnapUiState.Analyzing(bitmap)
                when (val ocrResult = container.ocrProcessor.extractText(uri)) {
                    is OCRProcessor.OcrResult.Success -> {
                        if (ocrResult.text.isBlank()) {
                            _uiState.value = FlowSnapUiState.Error("No readable text was found in that image.")
                        } else {
                            _uiState.value = FlowSnapUiState.Success(
                                bitmap,
                                FlowSnapIntelligenceEngine.analyze(ocrResult.text),
                            )
                        }
                    }
                    else -> _uiState.value = FlowSnapUiState.Error("FlowOS couldn't read the selected image.")
                }
            } catch (_: Exception) {
                _uiState.value = FlowSnapUiState.Error("Flow Snap could not analyze that image. Please try another image.")
            }
        }
    }

    fun saveToFlowSpace(insight: FlowSnapIntelligenceEngine.SnapInsight) {
        viewModelScope.launch {
            container.repository.saveCapture(
                container.repository.captureEntity(
                    id = "snap_${UUID.randomUUID()}",
                    sourceType = SourceType.IMAGE,
                    rawText = insight.summary,
                    attachmentPath = null,
                    summary = insight.summary,
                    intent = IntentType.UNKNOWN,
                    priority = null,
                    confidence = 1.0,
                    processingMode = "on-device-ocr",
                    people = emptyList(),
                    deadlineLabels = insight.detectedDate?.let { listOf(it.toString()) } ?: emptyList(),
                    projectId = null,
                ),
            )
            container.repository.logActivity("SNAP_SAVED", insight.summary, insight.category)
        }
    }

    fun createOutcome(insight: FlowSnapIntelligenceEngine.SnapInsight) {
        viewModelScope.launch {
            val priority = if (insight.category == "DEADLINE" || insight.category == "EXAM") {
                Priority.HIGH
            } else {
                Priority.MEDIUM
            }
            container.repository.createOutcome(
                title = insight.summary,
                deadline = insight.detectedDate,
                priority = priority,
            )
            saveToFlowSpace(insight)
        }
    }

    fun createStudyPlan(insight: FlowSnapIntelligenceEngine.SnapInsight) {
        viewModelScope.launch {
            val outcomeId = container.repository.createOutcome(
                title = "Prepare: ${insight.summary}",
                deadline = insight.detectedDate,
                priority = Priority.HIGH,
            )
            container.repository.createTask(
                title = "Review material for ${insight.summary}",
                outcomeId = outcomeId,
                deadlineEpochMillis = insight.detectedDate,
                priority = Priority.HIGH,
                estimatedDurationMinutes = 60,
            )
            saveToFlowSpace(insight)
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
