package com.flowos.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flowos.app.action.ActionBundle
import com.flowos.app.action.ActionBundleBuilder
import com.flowos.app.action.ActionKind
import com.flowos.app.action.ActionResult
import com.flowos.app.action.prepSummaryText
import com.flowos.app.di.AppContainer
import com.flowos.app.data.local.CaptureEntity
import com.flowos.app.data.local.FlowScoreEntity
import com.flowos.app.data.local.ProjectEntity
import com.flowos.app.data.local.TaskEntity
import com.flowos.app.domain.model.CalendarEventModel
import com.flowos.app.domain.model.EventPreparation
import com.flowos.app.domain.model.NextBestAction
import com.flowos.app.domain.model.VerificationState
import com.flowos.app.domain.model.WorkState
import com.flowos.app.domain.model.FlowScore
import com.flowos.app.planner.AdaptivePlanner
import com.flowos.app.planner.AdaptiveReplanner
import com.flowos.app.pulse.FrictionRadar
import com.flowos.app.pulse.TaskDependencyEdge
import com.flowos.app.pulse.ThreadOrdering
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** FlowPulse-derived state for the AI-first Home dashboard. */
data class HomePulseState(
    val workState: WorkState? = null,
    val todayTasks: List<TaskEntity> = emptyList(),
    val loading: Boolean = true,
    val flowScore: FlowScoreEntity? = null,
    val frictionAlerts: List<FrictionRadar.FrictionAlert> = emptyList(),
    val replanProposal: AdaptiveReplanner.ReplanProposal? = null,
)

class HomeViewModel(
    application: Application,
    private val container: AppContainer,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HomePulseState())
    val uiState: StateFlow<HomePulseState> = _uiState.asStateFlow()

    private var cachedEvents: List<CalendarEventModel> = emptyList()
    private var cachedProjects: List<ProjectEntity> = emptyList()
    private var cachedTasks: List<TaskEntity> = emptyList()
    private var cachedEdges: List<TaskDependencyEdge> = emptyList()

    init {
        viewModelScope.launch {
            combine(
                container.repository.observeProjects(),
                container.repository.observeAllTasks(),
                container.repository.observeDependencies(),
                container.repository.observeLatestScore(),
            ) { projects, tasks, deps, score -> Quadruple(projects, tasks, deps, score) }
                .collect { (projects, tasks, deps, score) ->
                    cachedProjects = projects
                    cachedTasks = tasks
                    cachedEdges = deps.map { TaskDependencyEdge(it.fromTaskId, it.toTaskId, it.reason) }
                    cachedScore = score
                    recompute()
                }
        }
        // One calendar read on entry — no continuous polling (privacy + perf).
        refreshCalendar()
    }

    private var cachedScore: FlowScoreEntity? = null

    fun refreshCalendar() {
        viewModelScope.launch {
            cachedEvents = container.calendarRepository.readUpcomingEvents(System.currentTimeMillis())
            recompute()
        }
    }

    private fun recompute() {
        val now = System.currentTimeMillis()
        val workState = container.workStateEngine.compute(
            projects = cachedProjects,
            tasks = cachedTasks,
            dependencies = cachedEdges,
            calendarEvents = cachedEvents,
            nowMillis = now,
        )
        
        val friction = FrictionRadar.detect(cachedTasks, cachedEdges, cachedEvents, now)
        
        val zone = ZoneId.systemDefault()
        val startOfDay = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
        val endOfDay = LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val todayTasks = workState.openTasks.filter {
            it.deadlineEpochMillis != null && it.deadlineEpochMillis in startOfDay until endOfDay
        }

        val plannerPlan = AdaptivePlanner.createPlan(cachedTasks, cachedEdges, cachedEvents, now)
        val proposal = if (friction.isNotEmpty()) {
            AdaptiveReplanner.propose(friction, plannerPlan, cachedTasks)
        } else null

        _uiState.value = HomePulseState(
            workState = workState,
            todayTasks = todayTasks,
            loading = false,
            flowScore = cachedScore,
            frictionAlerts = friction,
            replanProposal = proposal,
        )
    }

    private data class Quadruple<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

    fun completeTask(taskId: String) {
        viewModelScope.launch { container.repository.completeTask(taskId) }
    }
}

// ---- Plan -------------------------------------------------------------------

data class PlanEntry(
    val timeMillis: Long,
    val title: String,
    val isEvent: Boolean,
    val priority: com.flowos.app.domain.model.Priority?,
)

data class PlanDay(
    val dayLabel: String,
    val entries: List<PlanEntry>,
)

data class PlanUiState(
    val loading: Boolean = true,
    val today: List<PlanEntry> = emptyList(),
    val upcoming: List<PlanDay> = emptyList(),
    val preparations: List<EventPreparation> = emptyList(),
    val deadlines: List<TaskEntity> = emptyList(),
    val calendarConnected: Boolean = false,
    val activeBundle: ActionBundle? = null,
    val bundleApproved: Boolean = false,
    val bundleResults: List<String> = emptyList(),
)

class PlanViewModel(
    application: Application,
    private val container: AppContainer,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PlanUiState())
    val uiState: StateFlow<PlanUiState> = _uiState.asStateFlow()

    private var cachedEvents: List<CalendarEventModel> = emptyList()
    private var cachedProjects: List<ProjectEntity> = emptyList()
    private var cachedTasks: List<TaskEntity> = emptyList()
    private var pendingPrep: EventPreparation? = null

    init {
        viewModelScope.launch {
            combine(
                container.repository.observeProjects(),
                container.repository.observeAllTasks(),
            ) { projects, tasks -> projects to tasks }
                .collect { (projects, tasks) ->
                    cachedProjects = projects
                    cachedTasks = tasks
                    recompute()
                }
        }
        refreshCalendar()
    }

    /** Reads the calendar only when the user has connected it. */
    fun refreshCalendar() {
        viewModelScope.launch {
            cachedEvents = container.calendarRepository.readUpcomingEvents(System.currentTimeMillis())
            recompute()
        }
    }

    val calendarConnected: Boolean get() = container.calendarRepository.hasReadPermission()

    private fun recompute() {
        val now = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val startOfDay = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val endOfDay = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        val preparations = container.calendarIntelligenceEngine.preparationsFor(
            events = cachedEvents,
            projects = cachedProjects,
            tasks = cachedTasks,
            nowMillis = now,
        )

        val scheduled = cachedTasks
            .filter {
                it.status == com.flowos.app.domain.model.TaskStatus.ACTIVE.name &&
                    it.deadlineEpochMillis != null
            }
            .map { it to (it.deadlineEpochMillis ?: 0L) }

        val todayEntries = buildList {
            cachedEvents.filter { it.beginMillis in startOfDay until endOfDay }
                .forEach { add(PlanEntry(it.beginMillis, it.title, isEvent = true, priority = null)) }
            scheduled.filter { it.second in startOfDay until endOfDay }
                .forEach { (task, deadline) ->
                    add(
                        PlanEntry(
                            deadline,
                            task.title,
                            isEvent = false,
                            priority = com.flowos.app.domain.model.Priority.from(task.priority),
                        ),
                    )
                }
        }.sortedBy { it.timeMillis }

        val upcomingEntries = buildList {
            cachedEvents.filter { it.beginMillis >= endOfDay }
                .forEach { add(PlanEntry(it.beginMillis, it.title, isEvent = true, priority = null)) }
            scheduled.filter { it.second >= endOfDay }
                .forEach { (task, deadline) ->
                    add(
                        PlanEntry(
                            deadline,
                            task.title,
                            isEvent = false,
                            priority = com.flowos.app.domain.model.Priority.from(task.priority),
                        ),
                    )
                }
        }

        val upcomingDays = upcomingEntries
            .groupBy { Instant.ofEpochMilli(it.timeMillis).atZone(zone).toLocalDate() }
            .toSortedMap(compareBy { date -> date.toEpochDay() })
            .map { (date, entries) -> PlanDay(dayLabelOf(date, today), entries.sortedBy { it.timeMillis }) }

        _uiState.value = _uiState.value.copy(
            loading = false,
            today = todayEntries,
            upcoming = upcomingDays,
            preparations = preparations,
            deadlines = scheduled.map { it.first }.sortedBy { it.deadlineEpochMillis }.take(6),
            calendarConnected = calendarConnected,
        )
    }

    fun prepareForEvent(prep: EventPreparation) {
        pendingPrep = prep
        _uiState.value = _uiState.value.copy(
            activeBundle = ActionBundleBuilder.buildPreparationBundle(prep),
            bundleApproved = false,
            bundleResults = emptyList(),
        )
    }

    fun dismissBundle() {
        pendingPrep = null
        _uiState.value = _uiState.value.copy(
            activeBundle = null,
            bundleApproved = false,
            bundleResults = emptyList(),
        )
    }

    /** Executes every approved bundle item with real mechanisms; results are honest. */
    fun approveBundle() {
        val bundle = _uiState.value.activeBundle ?: return
        val prep = pendingPrep
        if (_uiState.value.bundleApproved) return

        viewModelScope.launch {
            val results = mutableListOf<String>()

            bundle.items.forEach { item ->
                val outcome: String = when (item.kind) {
                    ActionKind.CREATE_TASK -> {
                        container.repository.createTask(
                            title = item.title,
                            description = "Created from prep bundle: ${bundle.title}",
                            projectId = prep?.projectId,
                            deadlineEpochMillis = prep?.event?.beginMillis,
                            deadlineLabel = prep?.event?.let { eventLabel(it.beginMillis) },
                        )
                        "✓ Task created"
                    }

                    ActionKind.CREATE_REMINDER -> {
                        val at = item.atMillis ?: prep?.event?.beginMillis
                        if (at == null) {
                            "⚠ Reminder skipped — no time resolved"
                        } else {
                            when (val r = container.actionEngine.scheduleReminder(item.title, at)) {
                                is ActionResult.Success -> "✓ Reminder scheduled"
                                is ActionResult.Failed -> "⚠ ${r.reason}"
                            }
                        }
                    }

                    ActionKind.SCHEDULE_EVENT -> {
                        when (val r = container.actionEngine.scheduleCalendarEvent(
                            item.title,
                            item.atMillis ?: prep?.event?.beginMillis ?: System.currentTimeMillis(),
                        )) {
                            is ActionResult.Success -> "✓ ${r.message}"
                            is ActionResult.Failed -> "⚠ ${r.reason}"
                        }
                    }

                    ActionKind.SHARE_TEXT -> {
                        val shareBody = prep?.let { prepSummaryText(it, it.prepTasks) } ?: bundle.title
                        when (val r = container.actionEngine.shareText(shareBody)) {
                            is ActionResult.Success -> "✓ ${r.message}"
                            is ActionResult.Failed -> "⚠ ${r.reason}"
                        }
                    }

                    ActionKind.OPEN_FILE -> "⚠ No file linked to this bundle"
                }
                results += "${item.title}: $outcome"
            }

            container.repository.logActivity(
                "ACTION_BUNDLE_EXECUTED",
                bundle.title,
                results.joinToString("; "),
            )
            _uiState.value = _uiState.value.copy(bundleApproved = true, bundleResults = results)
        }
    }

    private fun eventLabel(beginMillis: Long): String =
        TimeLabel.format(beginMillis)

    private fun dayLabelOf(date: LocalDate, today: LocalDate): String = when (date) {
        today -> "TODAY"
        today.plusDays(1) -> "TOMORROW"
        else -> date.format(DayFormatter.formatter).uppercase(Locale.US)
    }

    private object TimeLabel {
        val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
        fun format(millis: Long): String =
            Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(formatter)
    }

    private object DayFormatter {
        val formatter = DateTimeFormatter.ofPattern("EEE d MMM", Locale.US)
    }
}

// ---- Flow (dependency graph) ---------------------------------------------------

data class FlowUiState(
    val projectName: String? = null,
    val nodes: List<TaskEntity> = emptyList(),
    val blockedIds: Set<String> = emptySet(),
    val nextTaskId: String? = null,
    val deadlineCount: Int = 0,
    val dependencyCount: Int = 0,
    val isEmpty: Boolean = true,
)

class FlowViewModel(
    application: Application,
    private val container: AppContainer,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(FlowUiState())
    val uiState: StateFlow<FlowUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                container.repository.observeProjects(),
                container.repository.observeAllTasks(),
                container.repository.observeDependencies(),
            ) { projects, tasks, deps -> Triple(projects, tasks, deps) }
                .collect { (projects, tasks, deps) ->
                    val now = System.currentTimeMillis()
                    val edges = deps.map { TaskDependencyEdge(it.fromTaskId, it.toTaskId, it.reason) }
                    val workState: WorkState = container.workStateEngine.compute(
                        projects = projects,
                        tasks = tasks,
                        dependencies = edges,
                        calendarEvents = emptyList(),
                        nowMillis = now,
                    )
                    val thread = workState.activeThread
                    if (thread == null) {
                        _uiState.value = FlowUiState(isEmpty = true)
                    } else {
                        val threadIds = thread.openTasks.map { it.id }.toSet()
                        val threadEdges = edges.filter { it.fromTaskId in threadIds && it.toTaskId in threadIds }
                        val ordered = ThreadOrdering.order(thread.openTasks, threadEdges)
                        _uiState.value = FlowUiState(
                            projectName = thread.projectName,
                            nodes = ordered,
                            blockedIds = thread.blockedTasks.map { it.id }.toSet(),
                            nextTaskId = workState.nextBestAction?.taskId,
                            deadlineCount = thread.openTasks.count { it.deadlineEpochMillis != null },
                            dependencyCount = threadEdges.size,
                            isEmpty = false,
                        )
                    }
                }
        }
    }
}

// ---- Focus ------------------------------------------------------------------

data class FocusUiState(
    val projectName: String? = null,
    val chainTitles: List<String> = emptyList(),
    val currentTask: TaskEntity? = null,
    val stepIndex: Int = 0,
    val totalSteps: Int = 0,
    val finished: Boolean = false,
    val timerActive: Boolean = false,
    val elapsedSeconds: Long = 0,
    val statusMessage: String? = null,
    val proofAttached: Boolean = false,
    val proofType: String? = null,
)

class FocusViewModel(
    application: Application,
    private val container: AppContainer,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    private var orderedOpen: List<TaskEntity> = emptyList()
    private var projectName: String? = null
    private val skippedIds = mutableSetOf<String>()
    
    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            combine(
                container.repository.observeProjects(),
                container.repository.observeAllTasks(),
                container.repository.observeDependencies(),
            ) { projects, tasks, deps -> Triple(projects, tasks, deps) }
                .collect { (projects, tasks, deps) ->
                    val now = System.currentTimeMillis()
                    val edges = deps.map { TaskDependencyEdge(it.fromTaskId, it.toTaskId, it.reason) }
                    val workState = container.workStateEngine.compute(
                        projects = projects,
                        tasks = tasks,
                        dependencies = edges,
                        calendarEvents = emptyList(),
                        nowMillis = now,
                    )
                    val thread = workState.activeThread
                    projectName = thread?.projectName
                    orderedOpen = if (thread == null) {
                        emptyList()
                    } else {
                        val threadIds = thread.openTasks.map { it.id }.toSet()
                        val threadEdges = edges.filter { it.fromTaskId in threadIds && it.toTaskId in threadIds }
                        ThreadOrdering.order(thread.openTasks, threadEdges)
                    }
                    recomputeFocus()
                }
        }
    }

    private fun recomputeFocus() {
        val remaining = orderedOpen.filter { it.id !in skippedIds }
        val current = remaining.firstOrNull()
        
        // Stop timer if task changed
        if (current?.id != _uiState.value.currentTask?.id) {
            stopTimer()
        }

        _uiState.value = _uiState.value.copy(
            projectName = projectName,
            chainTitles = orderedOpen.map { it.title },
            currentTask = current,
            stepIndex = orderedOpen.indexOfFirst { it.id == current?.id } + 1,
            totalSteps = orderedOpen.size,
            finished = current == null,
            statusMessage = _uiState.value.statusMessage.takeIf { current != null },
            proofAttached = false,
            proofType = null
        )
    }

    fun startTimer() {
        if (_uiState.value.timerActive) return
        _uiState.value = _uiState.value.copy(timerActive = true)
        
        val task = _uiState.value.currentTask ?: return
        if (task.startedAt == null) {
            viewModelScope.launch {
                container.repository.updateTask(task.copy(startedAt = System.currentTimeMillis()))
            }
        }

        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.value = _uiState.value.copy(elapsedSeconds = _uiState.value.elapsedSeconds + 1)
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(timerActive = false)
    }

    /** Completes the current task through the repository; the flow re-emits. */
    fun completeCurrent() {
        val task = _uiState.value.currentTask ?: return
        stopTimer()
        viewModelScope.launch {
            container.repository.completeTask(task.id)
            // Update actual duration
            val actualMin = (_uiState.value.elapsedSeconds / 60).toInt()
            container.repository.updateTask(task.copy(
                completedAt = System.currentTimeMillis(),
                actualDurationMinutes = actualMin,
                verificationState = VerificationState.COMPLETED.name
            ))
            _uiState.value = _uiState.value.copy(statusMessage = "Completed: ${task.title}", elapsedSeconds = 0)
        }
    }

    fun attachProof(type: String, uri: String? = null) {
        val task = _uiState.value.currentTask ?: return
        viewModelScope.launch {
            container.repository.saveEvidence(task.id, type, "Focus Mode", uri)
            container.repository.updateTask(task.copy(
                verificationState = VerificationState.VERIFIED.name
            ))
            _uiState.value = _uiState.value.copy(
                proofAttached = true,
                proofType = type,
                statusMessage = "Proof attached: $type. Outcome VERIFIED."
            )
        }
    }

    /** Skips without completing — nothing is claimed as done. */
    fun skipCurrent() {
        val task = _uiState.value.currentTask ?: return
        skippedIds += task.id
        recomputeFocus()
        _uiState.value = _uiState.value.copy(statusMessage = "Skipped: ${task.title}")
    }

    /** Opens the linked file when one exists; otherwise offers the share sheet. */
    fun openCurrent() {
        val task = _uiState.value.currentTask ?: return
        viewModelScope.launch {
            val capture: CaptureEntity? = task.sourceCaptureId?.let { container.repository.getCapture(it) }
            val path = capture?.attachmentPath
            val result = when {
                path != null -> container.actionEngine.openFile(path)
                else -> container.actionEngine.shareText("${task.title} — ${task.description}")
            }
            _uiState.value = _uiState.value.copy(
                statusMessage = when (result) {
                    is ActionResult.Success -> result.message
                    is ActionResult.Failed -> result.reason
                },
            )
        }
    }
}

// ---- Context graph ------------------------------------------------------------

data class ContextGraphUiState(
    val projectName: String? = null,
    val completionPercentage: Int = 0,
    val openTasks: List<TaskEntity> = emptyList(),
    val completedCount: Int = 0,
    val people: List<String> = emptyList(),
    val deadlines: List<TaskEntity> = emptyList(),
    val files: List<String> = emptyList(),
    val events: List<CalendarEventModel> = emptyList(),
    val captures: List<CaptureEntity> = emptyList(),
    val nextBestAction: NextBestAction? = null,
)

class ContextGraphViewModel(
    application: Application,
    private val container: AppContainer,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ContextGraphUiState())
    val uiState: StateFlow<ContextGraphUiState> = _uiState.asStateFlow()

    private var cachedEvents: List<CalendarEventModel> = emptyList()

    init {
        viewModelScope.launch {
            combine(
                container.repository.observeProjects(),
                container.repository.observeAllTasks(),
                container.repository.observeDependencies(),
                container.repository.observePeople(),
                container.repository.observeRecentCaptures(limit = 8),
            ) { projects, tasks, deps, people, captures ->
                ContextInputs(projects, tasks, deps, people.map { it.name }, captures)
            }.collect { inputs ->
                val now = System.currentTimeMillis()
                val edges = inputs.deps.map { TaskDependencyEdge(it.fromTaskId, it.toTaskId, it.reason) }
                val workState = container.workStateEngine.compute(
                    projects = inputs.projects,
                    tasks = inputs.tasks,
                    dependencies = edges,
                    calendarEvents = cachedEvents,
                    nowMillis = now,
                )
                val thread = workState.activeThread
                _uiState.value = ContextGraphUiState(
                    projectName = thread?.projectName,
                    completionPercentage = thread?.completionPercentage ?: 0,
                    openTasks = thread?.openTasks ?: emptyList(),
                    completedCount = inputs.tasks.count {
                        it.status == com.flowos.app.domain.model.TaskStatus.DONE.name &&
                            thread != null && it.projectId == thread.projectId
                    },
                    people = thread?.relatedPeople ?: emptyList(),
                    deadlines = thread?.upcomingDeadlines ?: emptyList(),
                    files = thread?.relatedFiles ?: emptyList(),
                    events = thread?.calendarEvents ?: emptyList(),
                    captures = inputs.captures,
                    nextBestAction = workState.nextBestAction,
                )
            }
        }
        viewModelScope.launch {
            cachedEvents = container.calendarRepository.readUpcomingEvents(System.currentTimeMillis())
        }
    }

    private data class ContextInputs(
        val projects: List<ProjectEntity>,
        val tasks: List<TaskEntity>,
        val deps: List<com.flowos.app.data.local.TaskDependencyEntity>,
        val people: List<String>,
        val captures: List<CaptureEntity>,
    )
}
