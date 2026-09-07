package com.flowos.app.data.demo

import com.flowos.app.data.local.ProjectEntity
import com.flowos.app.data.local.TaskDependencyEntity
import com.flowos.app.data.repository.FlowOSRepository
import com.flowos.app.domain.model.Priority
import com.flowos.app.domain.model.TaskStatus
import com.flowos.app.settings.SettingsStore
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Flagship demo content: the Architecture Review project.
 * Everything the FlowPulse engines compute (completion %, blocked tasks,
 * next best action, event preparation) is derived from THIS data — no
 * hardcoded numbers in the UI.
 */
class DemoDataSeeder(
    private val repository: FlowOSRepository,
    private val settingsStore: SettingsStore,
) {

    suspend fun seedIfFirstLaunch() {
        val seeded = settingsStore.demoSeeded.first()
        // Also reseed when the database is empty (e.g. after a destructive
        // schema migration) so the app never opens on a blank slate.
        if (!seeded || repository.taskCount() == 0) {
            seedDemoData()
            settingsStore.markDemoSeeded()
        }
    }

    suspend fun resetToDemoData() {
        repository.clearAllData()
        settingsStore.clearAll()
        seedDemoData()
        settingsStore.markDemoSeeded()
    }

    private suspend fun seedDemoData() {
        val now = System.currentTimeMillis()
        val today = LocalDate.now()
        val at = { dayOffset: Long, hour: Int, minute: Int ->
            LocalDateTime.of(today.plusDays(dayOffset), LocalTime.of(hour, minute))
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }

        val projectId = "project_architecture_review"
        repository.upsertProject(
            ProjectEntity(
                id = projectId,
                name = "Architecture Review",
                colorHex = "#FFD400",
                progressPercent = 0, // completion is now derived from tasks
                createdAt = now,
            ),
        )
        repository.rememberPerson("Prakash")

        // ---- Open work (the dependency chain drives Focus + Flow + Pulse) ----
        val finishDiagram = repository.createTask(
            title = "Finish architecture diagram",
            description = "Complete the visual layout for the new system architecture.",
            priority = Priority.HIGH,
            deadlineEpochMillis = at(0, 20, 0),
            deadlineLabel = "Tonight",
            projectId = projectId,
            orderIndex = 0,
        )
        val sendDocument = repository.createTask(
            title = "Send updated document to Prakash",
            description = "Export and share the architecture PDF with Prakash via email.",
            priority = Priority.HIGH,
            deadlineEpochMillis = at(0, 21, 0),
            deadlineLabel = "Tonight",
            personName = "Prakash",
            projectId = projectId,
            orderIndex = 1,
        )
        val prepareNotes = repository.createTask(
            title = "Prepare review notes",
            description = "Draft the key talking points for the upcoming project review meeting.",
            priority = Priority.MEDIUM,
            deadlineEpochMillis = at(1, 9, 0),
            deadlineLabel = "Tomorrow · 9:00 AM",
            projectId = projectId,
            orderIndex = 2,
        )
        val reviewEvent = repository.createTask(
            title = "Architecture Review",
            description = "Formal review session for the architecture diagram.",
            priority = Priority.MEDIUM,
            deadlineEpochMillis = at(1, 10, 0),
            deadlineLabel = "Tomorrow · 10:00 AM",
            projectId = projectId,
            orderIndex = 3,
        )

        // The confirmed dependency chain: diagram → document → notes → review.
        repository.saveDependencies(
            listOf(
                TaskDependencyEntity(
                    id = "dep_demo_1",
                    fromTaskId = finishDiagram,
                    toTaskId = sendDocument,
                    reason = "The document is generated from the diagram",
                    createdAt = now,
                ),
                TaskDependencyEntity(
                    id = "dep_demo_2",
                    fromTaskId = sendDocument,
                    toTaskId = prepareNotes,
                    reason = "Notes reference the sent document",
                    createdAt = now,
                ),
                TaskDependencyEntity(
                    id = "dep_demo_3",
                    fromTaskId = prepareNotes,
                    toTaskId = reviewEvent,
                    reason = "Notes must be ready for the review",
                    createdAt = now,
                ),
            ),
        )

        // ---- Completed history (one prep task + archived work) ---------------
        seedCompleted(repository, projectId, "Outline review agenda", at(0, 9, 0), now - 3 * DAY_MILLIS)
        seedCompleted(repository, projectId, "Idea finalized", null, now - 5 * DAY_MILLIS)
        seedCompleted(repository, projectId, "Stakeholder alignment", null, now - 4 * DAY_MILLIS)
        seedCompleted(repository, projectId, "Requirements gathering", null, now - 4 * DAY_MILLIS + MILLIS)
        seedCompleted(repository, projectId, "Initial draft", null, now - 3 * DAY_MILLIS)
        seedCompleted(repository, projectId, "Tech stack selection", null, now - 3 * DAY_MILLIS + MILLIS)
        seedCompleted(repository, projectId, "Wireframes reviewed", null, now - 2 * DAY_MILLIS)
        seedCompleted(repository, projectId, "Timeline planning", null, now - 2 * DAY_MILLIS + MILLIS)
        seedCompleted(repository, projectId, "Data flow draft", null, now - 1 * DAY_MILLIS)

        repository.logActivity("WORKFLOW_GENERATED", "Demo Workflow Created", "Architecture Review")
    }

    private suspend fun seedCompleted(
        repository: FlowOSRepository,
        projectId: String,
        title: String,
        deadlineMillis: Long?,
        finishedAt: Long,
    ) {
        val id = repository.createTask(
            title = title,
            projectId = projectId,
            priority = Priority.MEDIUM,
            deadlineEpochMillis = deadlineMillis,
        )
        repository.getTask(id)?.let { task ->
            repository.updateTask(
                task.copy(
                    status = TaskStatus.DONE.name,
                    completedAt = finishedAt,
                ),
            )
        }
    }

    private companion object {
        const val DAY_MILLIS = 24 * 60 * 60 * 1000L
        const val MILLIS = 60 * 60 * 1000L
    }
}
