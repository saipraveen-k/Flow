package com.flowos.app.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flowos.app.action.ActionDescriptor
import com.flowos.app.action.ActionEngine
import com.flowos.app.action.ActionKind
import com.flowos.app.action.ActionResult
import com.flowos.app.capture.DocumentExtractor
import com.flowos.app.capture.OCRProcessor
import com.flowos.app.capture.VoiceState
import com.flowos.app.data.local.ActivityEventEntity
import com.flowos.app.data.local.CaptureEntity
import com.flowos.app.data.local.ProjectEntity
import com.flowos.app.data.local.TaskEntity
import com.flowos.app.di.AppContainer
import com.flowos.app.domain.model.AIAnalysisResult
import com.flowos.app.domain.model.CaptureDraft
import com.flowos.app.domain.model.SourceType
import com.flowos.app.settings.AiMode
import com.flowos.app.domain.model.Outcome
import com.flowos.app.domain.model.Strategy
import com.flowos.app.workflow.OutcomeCompiler
import com.flowos.app.workflow.WorkflowPlanner
import com.flowos.app.domain.model.WorkflowPlan
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Holds everything flowing through the capture → execute loop so screens stay
 * stateless and restart-safe within a session. Session-only by design; the
 * database remains the source of truth across restarts.
 */
class CaptureSession {
    var draft: CaptureDraft? = null
    var analysis: AIAnalysisResult? = null
    var outcome: Outcome? = null
    var processingMode: String = ""
    var workflowPlan: WorkflowPlan? = null
    var createdTaskIds: List<String> = emptyList()
    var createdWorkflowId: String? = null
}

sealed interface CaptureUiState {
    data object Idle : CaptureUiState
    data object Listening : CaptureUiState
    data object Processing : CaptureUiState
    data class Ready(val text: String, val sourceType: SourceType, val attachmentPath: String?) : CaptureUiState
    data class Error(val message: String) : CaptureUiState
}

class CaptureViewModel(
    application: Application,
    private val container: AppContainer,
    private val session: CaptureSession,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<CaptureUiState>(CaptureUiState.Idle)
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    private var listenJob: Job? = null

    val speechAvailable: Boolean by lazy { container.speechEngine().isAvailable() }

    fun startVoiceCapture() {
        if (_uiState.value is CaptureUiState.Listening) return
        listenJob?.cancel()
        listenJob = viewModelScope.launch {
            val engine = container.speechEngine()
            engine.listen().collect { state ->
                when (state) {
                    is VoiceState.Listening -> _uiState.value = CaptureUiState.Listening
                    is VoiceState.Processing -> _uiState.value = CaptureUiState.Processing
                    is VoiceState.Result -> {
                        session.draft = CaptureDraft(sourceType = SourceType.VOICE, text = state.text)
                        _uiState.value = CaptureUiState.Ready(state.text, SourceType.VOICE, null)
                    }

                    is VoiceState.Error -> _uiState.value = CaptureUiState.Error(state.message)
                    VoiceState.Idle -> Unit
                }
            }
        }
    }

    fun stopVoiceCapture() {
        listenJob?.cancel()
        container.speechEngine().stop()
        if (_uiState.value is CaptureUiState.Listening) {
            _uiState.value = CaptureUiState.Idle
        }
    }

    fun submitTypedText(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            _uiState.value = CaptureUiState.Error("Type or say something first.")
            return
        }
        session.draft = CaptureDraft(sourceType = SourceType.TEXT, text = trimmed)
        _uiState.value = CaptureUiState.Ready(trimmed, SourceType.TEXT, null)
    }

    fun onImageCaptured(uri: Uri) {
        _uiState.value = CaptureUiState.Processing
        viewModelScope.launch {
            when (val ocr = container.ocrProcessor.extractText(uri)) {
                is OCRProcessor.OcrResult.Success -> {
                    val persisted = persistAttachment(uri)
                    session.draft = CaptureDraft(
                        sourceType = SourceType.IMAGE,
                        text = ocr.text,
                        attachmentPath = persisted,
                    )
                    _uiState.value = CaptureUiState.Ready(ocr.text, SourceType.IMAGE, persisted)
                }

                is OCRProcessor.OcrResult.Failure -> _uiState.value = CaptureUiState.Error(ocr.message)
            }
        }
    }

    fun onDocumentSelected(uri: Uri) {
        _uiState.value = CaptureUiState.Processing
        viewModelScope.launch {
            when (val result = container.documentExtractor.extract(uri)) {
                is DocumentExtractor.DocumentResult.Success -> {
                    session.draft = CaptureDraft(
                        sourceType = SourceType.DOCUMENT,
                        text = result.text,
                        attachmentPath = result.persistedPath,
                    )
                    _uiState.value = CaptureUiState.Ready(result.text, SourceType.DOCUMENT, result.persistedPath)
                }

                is DocumentExtractor.DocumentResult.Failure ->
                    _uiState.value = CaptureUiState.Error(result.message)
            }
        }
    }

    private suspend fun persistAttachment(uri: Uri): String? = try {
        val dir = java.io.File(getApplication<Application>().cacheDir, "captures").apply { mkdirs() }
        val file = java.io.File(dir, "capture_${System.currentTimeMillis()}.jpg")
        getApplication<Application>().contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        file.absolutePath
    } catch (_: Exception) {
        null
    }

    fun reset() {
        listenJob?.cancel()
        _uiState.value = CaptureUiState.Idle
    }

    fun setError(message: String) {
        _uiState.value = CaptureUiState.Error(message)
    }
}

data class ProcessingUiState(
    val stepIndex: Int = 0,
    val processingLabel: String = "",
    val finished: Boolean = false,
    val error: String? = null,
)

class ProcessingViewModel(
    application: Application,
    private val container: AppContainer,
    private val session: CaptureSession,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ProcessingUiState())
    val uiState: StateFlow<ProcessingUiState> = _uiState.asStateFlow()

    fun startProcessing() {
        val draft = session.draft ?: run {
            _uiState.value = ProcessingUiState(
                finished = true,
                error = "Nothing to process. Capture something first.",
            )
            return
        }

        viewModelScope.launch {
            val mode = when (container.settingsStore.aiMode.first()) {
                AiMode.LOCAL -> container.localAIEngine
                AiMode.DEMO -> container.mockAIEngine
            }
            _uiState.value = ProcessingUiState(processingLabel = mode.processingLabel)

            try {
                // Phase the UI through the pipeline while analysis runs.
                val analysisDeferred = async { mode.analyze(draft) }
                for (stepDelay in PROCESSING_STEP_DELAYS_MS) {
                    delay(stepDelay)
                    _uiState.value = _uiState.value.copy(stepIndex = _uiState.value.stepIndex + 1)
                }
                val rawAnalysis = analysisDeferred.await()
                val connected = container.contextEngine.connectContext(rawAnalysis)
                val compilation = OutcomeCompiler.compile(connected)
                
                session.analysis = connected
                session.outcome = compilation.outcome
                session.processingMode = mode.processingLabel
                session.workflowPlan = WorkflowPlanner.plan(connected)
                _uiState.value = _uiState.value.copy(finished = true)
            } catch (_: Exception) {
                _uiState.value = ProcessingUiState(
                    finished = true,
                    error = "FlowOS couldn't understand this capture. Try rephrasing or a clearer image.",
                )
            }
        }
    }

    companion object {
        val PROCESSING_STEP_DELAYS_MS = listOf(450L, 450L, 500L, 450L, 400L)
    }
}

class WorkflowViewModel(
    application: Application,
    private val container: AppContainer,
    private val session: CaptureSession,
) : AndroidViewModel(application) {

    /** Persists the plan into tasks + workflow rows exactly as displayed. */
    suspend fun persistWorkflow(): Boolean {
        val analysis = session.analysis ?: return false
        val plan = session.workflowPlan ?: WorkflowPlanner.plan(analysis)
        if (plan.steps.isEmpty()) return false

        val projectId = container.contextEngine.resolveProject(analysis)
        val captureId = "capture_${UUID.randomUUID()}"

        val taskIdByIndex = mutableMapOf<Int, String>()
        val taskIds = mutableListOf<String>()
        analysis.tasks.forEachIndexed { index, extracted ->
            val id = container.repository.createTask(
                title = extracted.title,
                description = extracted.description,
                priority = extracted.priority,
                deadlineEpochMillis = extracted.deadlineEpochMillis,
                deadlineLabel = extracted.deadlineLabel,
                personName = extracted.person,
                projectId = projectId,
                sourceCaptureId = captureId,
                orderIndex = index,
            )
            taskIdByIndex[index] = id
            taskIds.add(id)
        }

        container.repository.saveCapture(
            container.repository.captureEntity(
                id = captureId,
                sourceType = analysis.sourceType,
                rawText = session.draft?.text.orEmpty(),
                attachmentPath = session.draft?.attachmentPath,
                summary = analysis.summary,
                intent = analysis.intent,
                priority = analysis.priority,
                confidence = analysis.confidence,
                processingMode = session.processingMode,
                people = analysis.people,
                deadlineLabels = analysis.deadlines.map { it.label },
                projectId = projectId,
            ),
        )

        val workflowId = "workflow_${UUID.randomUUID()}"
        val steps = plan.steps.map { step ->
            val originalIndex = analysis.tasks.indices.firstOrNull {
                analysis.tasks[it].title == step.title
            } ?: plan.steps.indexOf(step)
            com.flowos.app.data.local.WorkflowStepEntity(
                id = "step_${UUID.randomUUID()}",
                workflowId = workflowId,
                taskId = taskIdByIndex[originalIndex] ?: taskIds.firstOrNull() ?: "",
                orderIndex = step.order,
                action = when {
                    analysis.tasks.getOrNull(originalIndex)?.requiresSharing == true ->
                        ActionKind.SHARE_TEXT.name

                    analysis.tasks.getOrNull(originalIndex)?.deadlineEpochMillis != null ->
                        ActionKind.CREATE_REMINDER.name

                    else -> ActionKind.CREATE_TASK.name
                },
                actionDetail = step.deadlineLabel.orEmpty(),
            )
        }

        val savedWorkflowId = container.repository.saveWorkflow(
            com.flowos.app.data.local.WorkflowEntity(
                id = workflowId,
                captureId = captureId,
                title = plan.title,
                status = "PENDING",
                createdAt = System.currentTimeMillis(),
            ),
            steps,
        )

        // Persist the confirmed dependency edges so FlowPulse can compute
        // blocked tasks and the Flow graph can render real chains.
        val dependencies = analysis.dependencies.mapNotNull { dep ->
            val fromId = taskIdByIndex[dep.fromIndex]
            val toId = taskIdByIndex[dep.toIndex]
            if (fromId != null && toId != null) {
                com.flowos.app.data.local.TaskDependencyEntity(
                    id = "dep_${UUID.randomUUID()}",
                    fromTaskId = fromId,
                    toTaskId = toId,
                    reason = dep.reason,
                    createdAt = System.currentTimeMillis(),
                )
            } else {
                null
            }
        }
        if (dependencies.isNotEmpty()) {
            container.repository.saveDependencies(dependencies)
        }

        session.createdTaskIds = taskIds
        session.createdWorkflowId = savedWorkflowId
        return true
    }
}

data class ExecuteUiState(
    val actions: List<ActionDescriptor> = emptyList(),
    val completedKinds: Set<ActionKind> = emptySet(),
    val running: Boolean = false,
    val finished: Boolean = false,
    val failures: List<String> = emptyList(),
)

class ExecuteViewModel(
    application: Application,
    private val container: AppContainer,
    private val session: CaptureSession,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ExecuteUiState())
    val uiState: StateFlow<ExecuteUiState> = _uiState.asStateFlow()

    fun prepare() {
        val analysis = session.analysis ?: return
        val hasEvent = analysis.deadlines.any { it.isEvent }
        _uiState.value = ExecuteUiState(
            actions = container.actionEngine.buildActions(analysis.tasks, includeCalendarEvent = hasEvent),
        )
    }

    fun execute() {
        if (_uiState.value.running || _uiState.value.finished) return
        _uiState.value = _uiState.value.copy(running = true)
        viewModelScope.launch {
            val failures = mutableListOf<String>()
            val completed = mutableSetOf<ActionKind>()
            val analysis = session.analysis
            val actionEngine: ActionEngine = container.actionEngine

            // 1. Tasks were persisted during workflow confirmation — report honestly.
            if (session.createdTaskIds.isNotEmpty()) {
                completed += ActionKind.CREATE_TASK
            }

            // 2. Schedule reminders for every deadline-bearing task.
            analysis?.tasks?.filter { it.deadlineEpochMillis != null }?.forEach { task ->
                task.deadlineEpochMillis?.let { deadline ->
                    when (val result = actionEngine.scheduleReminder(task.title, deadline)) {
                        is ActionResult.Success -> completed += ActionKind.CREATE_REMINDER
                        is ActionResult.Failed -> failures += result.reason
                    }
                }
            }

            // 3. Draft the calendar event (user confirms in their calendar app).
            analysis?.deadlines?.firstOrNull { it.isEvent }?.let { deadline ->
                when (val result = actionEngine.scheduleCalendarEvent(deadline.label, deadline.epochMillis)) {
                    is ActionResult.Success -> completed += ActionKind.SCHEDULE_EVENT
                    is ActionResult.Failed -> failures += result.reason
                }
            }

            // 4. Share-ready text for tasks that involve sending something.
            analysis?.tasks?.filter { it.requiresSharing }?.forEach { task ->
                when (val result = actionEngine.shareText("${task.title} — ${task.description}")) {
                    is ActionResult.Success -> completed += ActionKind.SHARE_TEXT
                    is ActionResult.Failed -> failures += result.reason
                }
            }

            session.createdWorkflowId?.let { container.repository.markWorkflowExecuted(it) }
            _uiState.value = ExecuteUiState(
                actions = _uiState.value.actions,
                completedKinds = completed,
                running = false,
                finished = true,
                failures = failures,
            )
        }
    }
}

data class ActivityUiState(
    val project: ProjectEntity? = null,
    val projectTasks: List<TaskEntity> = emptyList(),
    val nextDeadline: TaskEntity? = null,
    val recentCaptures: List<CaptureEntity> = emptyList(),
    val activityEvents: List<ActivityEventEntity> = emptyList()
)

class ActivityViewModel(
    application: Application,
    private val container: AppContainer,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val projectId = container.repository.getActiveProjectId()
            if (projectId != null) {
                container.repository.observeProject(projectId).collect { project ->
                    _uiState.value = _uiState.value.copy(project = project)
                }
            }
        }
        viewModelScope.launch {
            val projectId = container.repository.getActiveProjectId()
            if (projectId != null) {
                container.repository.observeProjectTasks(projectId).collect { tasks ->
                    _uiState.value = _uiState.value.copy(
                        projectTasks = tasks,
                        nextDeadline = tasks.filter { it.deadlineEpochMillis != null }
                            .minByOrNull { it.deadlineEpochMillis ?: Long.MAX_VALUE },
                    )
                }
            }
        }
        viewModelScope.launch {
            container.repository.observeRecentCaptures().collect { captures ->
                _uiState.value = _uiState.value.copy(recentCaptures = captures)
            }
        }
        viewModelScope.launch {
            container.repository.observeActivity().collect { events ->
                _uiState.value = _uiState.value.copy(activityEvents = events)
            }
        }
    }
}

class SettingsViewModel(
    application: Application,
    private val container: AppContainer,
) : AndroidViewModel(application) {

    val aiMode: StateFlow<AiMode> = container.settingsStore.aiMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AiMode.DEMO)

    val themeMode: StateFlow<com.flowos.app.settings.ThemeMode> = container.settingsStore.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), com.flowos.app.settings.ThemeMode.SYSTEM_DEFAULT)

    fun setAiMode(mode: AiMode) {
        viewModelScope.launch { container.settingsStore.setAiMode(mode) }
    }

    fun setThemeMode(mode: com.flowos.app.settings.ThemeMode) {
        viewModelScope.launch { container.settingsStore.setThemeMode(mode) }
    }

    /** Wipes all data and re-seeds the demo context so the app stays usable. */
    fun clearAllData(onDone: () -> Unit) {
        viewModelScope.launch {
            container.demoDataSeeder.resetToDemoData()
            onDone()
        }
    }
}

class StrategyViewModel(
    application: Application,
    private val container: AppContainer,
) : AndroidViewModel(application) {
    
    fun useStrategy(strategy: Strategy, onDone: () -> Unit) {
        viewModelScope.launch {
            container.repository.applyStrategy(strategy)
            onDone()
        }
    }
}
