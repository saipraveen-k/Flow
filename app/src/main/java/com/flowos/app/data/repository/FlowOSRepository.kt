package com.flowos.app.data.repository

import com.flowos.app.data.local.ActivityEventDao
import com.flowos.app.data.local.ActivityEventEntity
import com.flowos.app.data.local.CaptureDao
import com.flowos.app.data.local.CaptureEntity
import com.flowos.app.data.local.EvidenceDao
import com.flowos.app.data.local.EvidenceEntity
import com.flowos.app.data.local.FlowOSDatabase
import com.flowos.app.data.local.GoalDao
import com.flowos.app.data.local.GoalEntity
import com.flowos.app.data.local.OutcomeDao
import com.flowos.app.data.local.OutcomeEntity
import com.flowos.app.data.local.FlowScoreDao
import com.flowos.app.data.local.FlowScoreEntity
import com.flowos.app.data.local.PersonDao
import com.flowos.app.data.local.PersonEntity
import com.flowos.app.data.local.TaskDependencyDao
import com.flowos.app.data.local.TaskDependencyEntity
import com.flowos.app.data.local.ProjectDao
import com.flowos.app.data.local.ProjectEntity
import com.flowos.app.data.local.TaskDao
import com.flowos.app.data.local.TaskEntity
import com.flowos.app.data.local.WorkflowDao
import com.flowos.app.data.local.WorkflowEntity
import com.flowos.app.data.local.WorkflowStepEntity
import com.flowos.app.domain.model.IntentType
import com.flowos.app.domain.model.LifeHub
import com.flowos.app.domain.model.Priority
import com.flowos.app.domain.model.SourceType
import com.flowos.app.domain.model.Strategy
import com.flowos.app.domain.model.TaskStatus
import com.flowos.app.domain.model.VerificationState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Single gateway from ViewModels to persistence. Owns ID generation and
 * entity mapping so callers work with domain-friendly data only.
 */
class FlowOSRepository(
    private val database: FlowOSDatabase,
) {
    private val taskDao: TaskDao = database.taskDao()
    private val projectDao: ProjectDao = database.projectDao()
    private val personDao: PersonDao = database.personDao()
    private val captureDao: CaptureDao = database.captureDao()
    private val workflowDao: WorkflowDao = database.workflowDao()
    private val activityDao: ActivityEventDao = database.activityEventDao()
    private val dependencyDao: TaskDependencyDao = database.taskDependencyDao()
    private val goalDao: GoalDao = database.goalDao()
    private val outcomeDao: OutcomeDao = database.outcomeDao()
    private val evidenceDao: EvidenceDao = database.evidenceDao()
    private val flowScoreDao: FlowScoreDao = database.flowScoreDao()

    // ---- Tasks -----------------------------------------------------------

    fun observeActiveTasks(): Flow<List<TaskEntity>> = taskDao.observeActiveTasks()

    fun observeAllTasks(): Flow<List<TaskEntity>> = taskDao.observeAllTasks()

    fun observeCompletedTasks(limit: Int = 20): Flow<List<TaskEntity>> =
        taskDao.observeCompletedTasks(limit)

    fun observeUpcomingDeadlines(from: Long): Flow<List<TaskEntity>> =
        taskDao.observeUpcomingDeadlines(from, limit = 5)

    fun observeTaskById(taskId: String): Flow<TaskEntity?> = taskDao.observeById(taskId)

    fun observeHighPriorityCount(): Flow<Int> = taskDao.observeHighPriorityCount()

    fun observeActiveCount(): Flow<Int> = taskDao.observeActiveCount()

    fun observeCompletedCount(): Flow<Int> = taskDao.observeCompletedCount()

    suspend fun getTask(taskId: String): TaskEntity? = taskDao.getById(taskId)

    suspend fun createTask(
        title: String,
        description: String = "",
        priority: Priority = Priority.MEDIUM,
        deadlineEpochMillis: Long? = null,
        deadlineLabel: String? = null,
        personName: String? = null,
        projectId: String? = null,
        outcomeId: String? = null,
        hub: LifeHub = LifeHub.PROFESSIONAL,
        estimatedDurationMinutes: Int = 30,
        sourceCaptureId: String? = null,
        orderIndex: Int = 0,
    ): String = withContext(Dispatchers.IO) {
        val id = "task_${UUID.randomUUID()}"
        taskDao.insert(
            TaskEntity(
                id = id,
                title = title,
                description = description,
                status = TaskStatus.ACTIVE.name,
                priority = priority.name,
                deadlineEpochMillis = deadlineEpochMillis,
                deadlineLabel = deadlineLabel,
                personName = personName,
                projectId = projectId,
                outcomeId = outcomeId,
                hub = hub.name,
                estimatedDurationMinutes = estimatedDurationMinutes,
                sourceCaptureId = sourceCaptureId,
                orderIndex = orderIndex,
                createdAt = System.currentTimeMillis(),
                verificationState = VerificationState.PLANNED.name
            ),
        )
        id
    }

    suspend fun completeTask(taskId: String) = withContext(Dispatchers.IO) {
        taskDao.complete(taskId, System.currentTimeMillis())
        taskDao.getById(taskId)?.let { task ->
            logActivity("TASK_COMPLETED", "Task completed", task.title)
        }
    }

    suspend fun updateTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        taskDao.update(task)
    }

    // ---- Projects & people ------------------------------------------------

    fun observeProjects(): Flow<List<ProjectEntity>> = projectDao.observeAll()

    suspend fun observeProjectsOnce(): List<ProjectEntity> =
        withContext(Dispatchers.IO) { projectDao.observeAll().firstOrNull() ?: emptyList() }

    /** Most recently used project; treated as the active context. */
    suspend fun getActiveProjectId(): String? = observeProjectsOnce().firstOrNull()?.id

    fun observeProject(projectId: String): Flow<ProjectEntity?> = projectDao.observeById(projectId)

    suspend fun upsertProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        projectDao.insert(project)
    }

    suspend fun getProject(projectId: String): ProjectEntity? = projectDao.getById(projectId)

    suspend fun observeProjectTasksOnce(projectId: String): List<TaskEntity> =
        withContext(Dispatchers.IO) { taskDao.observeProjectTasks(projectId).firstOrNull() ?: emptyList() }

    fun observeProjectTasks(projectId: String): Flow<List<TaskEntity>> =
        taskDao.observeProjectTasks(projectId)

    suspend fun getAllProjectTasks(projectId: String): List<TaskEntity> =
        withContext(Dispatchers.IO) { taskDao.getAllByProject(projectId) }

    fun observePeople(): Flow<List<PersonEntity>> = personDao.observeAll()

    suspend fun rememberPerson(name: String) = withContext(Dispatchers.IO) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) {
            val existing = personDao.getByName(trimmed)
            personDao.insert(
                existing ?: PersonEntity(
                    id = "person_${UUID.randomUUID()}",
                    name = trimmed,
                    firstSeenAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    // ---- Captures ----------------------------------------------------------

    suspend fun saveCapture(entity: CaptureEntity): String = withContext(Dispatchers.IO) {
        captureDao.insert(entity)
        logActivity("CAPTURE_CREATED", "New capture saved", entity.summary ?: "Capture")
        entity.id
    }

    suspend fun getCapture(captureId: String): CaptureEntity? = captureDao.getById(captureId)

    suspend fun getLatestCapture(): CaptureEntity? = captureDao.getLatest()

    fun observeRecentCaptures(limit: Int = 30): Flow<List<CaptureEntity>> =
        captureDao.observeRecent(limit)

    // ---- Workflows ---------------------------------------------------------

    suspend fun saveWorkflow(
        workflow: WorkflowEntity,
        steps: List<WorkflowStepEntity>,
    ): String = withContext(Dispatchers.IO) {
        workflowDao.insertWithSteps(workflow, steps)
        logActivity("WORKFLOW_GENERATED", "Workflow created", workflow.title)
        workflow.id
    }

    suspend fun getLatestWorkflow(): WorkflowEntity? = workflowDao.getLatest()

    suspend fun markWorkflowExecuted(workflowId: String) = withContext(Dispatchers.IO) {
        workflowDao.markExecuted(workflowId, System.currentTimeMillis())
        workflowDao.getLatest()?.let { wf ->
            logActivity("WORKFLOW_EXECUTED", "Workflow executed", wf.title)
        }
    }

    suspend fun markStepCompleted(stepId: String, completed: Boolean) =
        withContext(Dispatchers.IO) { workflowDao.setStepCompleted(stepId, completed) }

    suspend fun getWorkflowSteps(workflowId: String): List<WorkflowStepEntity> =
        workflowDao.getSteps(workflowId)

    fun observeWorkflows(): Flow<List<WorkflowEntity>> = workflowDao.observeAll()

    // ---- Task dependencies --------------------------------------------------

    fun observeDependencies(): Flow<List<TaskDependencyEntity>> = dependencyDao.observeAll()

    suspend fun getAllDependencies(): List<TaskDependencyEntity> = dependencyDao.getAll()

    suspend fun getDependentsOf(taskId: String): List<TaskDependencyEntity> =
        dependencyDao.getDependentsOf(taskId)

    suspend fun saveDependencies(dependencies: List<TaskDependencyEntity>) =
        withContext(Dispatchers.IO) { dependencyDao.insertAll(dependencies) }

    // ---- Activity Events ---------------------------------------------------

    fun observeActivity(limit: Int = 50): Flow<List<ActivityEventEntity>> = activityDao.observeRecent(limit)

    suspend fun logActivity(type: String, title: String, detail: String? = null) = withContext(Dispatchers.IO) {
        activityDao.insert(
            ActivityEventEntity(
                id = "evt_${UUID.randomUUID()}",
                type = type,
                title = title,
                detail = detail,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    // ---- Goals & Outcomes --------------------------------------------------

    fun observeGoals(): Flow<List<GoalEntity>> = goalDao.observeAll()

    suspend fun createGoal(title: String, description: String = ""): String = withContext(Dispatchers.IO) {
        val id = "goal_${UUID.randomUUID()}"
        goalDao.insert(
            GoalEntity(
                id = id,
                title = title,
                description = description,
                status = "ACTIVE",
                createdAt = System.currentTimeMillis()
            )
        )
        id
    }

    fun observeOutcomes(): Flow<List<OutcomeEntity>> = outcomeDao.observeAll()

    fun observeOutcomesByHub(hub: LifeHub): Flow<List<OutcomeEntity>> = outcomeDao.observeByHub(hub.name)

    suspend fun createOutcome(
        title: String,
        description: String = "",
        goalId: String? = null,
        deadline: Long? = null,
        priority: Priority = Priority.MEDIUM,
        hub: LifeHub = LifeHub.PROFESSIONAL
    ): String = withContext(Dispatchers.IO) {
        val id = "outcome_${UUID.randomUUID()}"
        outcomeDao.insert(
            OutcomeEntity(
                id = id,
                title = title,
                description = description,
                goalId = goalId,
                deadlineEpochMillis = deadline,
                priority = priority.name,
                status = VerificationState.PLANNED.name,
                progressPercent = 0,
                hub = hub.name,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        id
    }

    suspend fun updateOutcome(outcome: OutcomeEntity) = withContext(Dispatchers.IO) {
        outcomeDao.update(outcome.copy(updatedAt = System.currentTimeMillis()))
    }

    // ---- Evidence & Scoring ------------------------------------------------

    fun observeEvidence(targetId: String): Flow<List<EvidenceEntity>> = evidenceDao.observeByTarget(targetId)

    suspend fun saveEvidence(
        targetId: String,
        type: String,
        source: String,
        uri: String? = null
    ) = withContext(Dispatchers.IO) {
        evidenceDao.insert(
            EvidenceEntity(
                id = "ev_${UUID.randomUUID()}",
                targetId = targetId,
                type = type,
                source = source,
                referenceUri = uri,
                timestamp = System.currentTimeMillis(),
                verificationState = VerificationState.EVIDENCE_ATTACHED.name
            )
        )
    }

    fun observeLatestScore(): Flow<FlowScoreEntity?> = flowScoreDao.observeLatest()

    suspend fun saveScore(score: FlowScoreEntity) = withContext(Dispatchers.IO) {
        flowScoreDao.insert(score)
    }

    suspend fun applyStrategy(strategy: Strategy, goalId: String? = null): String = withContext(Dispatchers.IO) {
        val outcomeId = createOutcome(
            title = strategy.title,
            description = strategy.description,
            goalId = goalId,
            priority = Priority.MEDIUM,
            hub = LifeHub.PROFESSIONAL
        )
        
        strategy.steps.forEachIndexed { index, step ->
            createTask(
                title = step.title,
                description = "Part of strategy: ${strategy.title}",
                priority = Priority.MEDIUM,
                outcomeId = outcomeId,
                estimatedDurationMinutes = step.estimatedDurationMinutes,
                orderIndex = index
            )
        }
        outcomeId
    }

    // ---- Maintenance -------------------------------------------------------

    /** Wipes every user table; used by "Clear all local data". */
    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        flowScoreDao.clearAll()
        evidenceDao.clearAll()
        outcomeDao.clearAll()
        goalDao.clearAll()
        activityDao.clearAll()
        workflowDao.clearSteps()
        workflowDao.clearAll()
        dependencyDao.clearAll()
        taskDao.clearAll()
        captureDao.clearAll()
        personDao.clearAll()
        projectDao.clearAll()
    }

    suspend fun captureCount(): Int = withContext(Dispatchers.IO) {
        captureDao.observeRecent(Int.MAX_VALUE).firstOrNull()?.size ?: 0
    }

    suspend fun taskCount(): Int = withContext(Dispatchers.IO) { taskDao.countAll() }

    // ---- Mapping helpers ---------------------------------------------------

    fun captureEntity(
        id: String,
        sourceType: SourceType,
        rawText: String,
        attachmentPath: String?,
        summary: String?,
        intent: IntentType,
        priority: Priority?,
        confidence: Double,
        processingMode: String,
        people: List<String>,
        deadlineLabels: List<String>,
        projectId: String?,
    ): CaptureEntity = CaptureEntity(
        id = id,
        sourceType = sourceType.name,
        rawText = rawText,
        attachmentPath = attachmentPath,
        summary = summary,
        intent = intent.name,
        priority = priority?.name,
        confidence = confidence,
        processingMode = processingMode,
        detectedPeople = people.joinToString(","),
        detectedDeadlines = deadlineLabels.joinToString(","),
        projectId = projectId,
        createdAt = System.currentTimeMillis(),
    )
}
