package com.flowos.app.calendar

import com.flowos.app.data.local.ProjectEntity
import com.flowos.app.data.local.TaskEntity
import com.flowos.app.domain.model.CalendarEventModel
import com.flowos.app.domain.model.EventPreparation
import com.flowos.app.domain.model.TaskStatus

/**
 * Associates calendar events with projects and derives preparation state.
 *
 * Matching is deterministic:
 *  1. event title contains a significant word (>= 5 chars) of the project name;
 *  2. otherwise the event starts within 12h of one of the project's deadlines.
 *
 * Readiness = completed prep tasks / all prep tasks due before the event —
 * derived from real task data, never invented.
 */
class CalendarIntelligenceEngine {

    fun matchProject(
        event: CalendarEventModel,
        projects: List<ProjectEntity>,
        tasks: List<TaskEntity>,
    ): ProjectEntity? {
        val titleLower = event.title.lowercase()

        projects.firstOrNull { project ->
            val words = project.name.lowercase().split(Regex("\\s+")).filter { it.length >= 5 }
            words.any { titleLower.contains(it) }
        }?.let { return it }

        return projects.firstOrNull { project ->
            tasks.any { task ->
                task.projectId == project.id &&
                    task.deadlineEpochMillis != null &&
                    Math.abs(task.deadlineEpochMillis - event.beginMillis) <= 12 * 60 * 60 * 1000L
            }
        }
    }

    /**
     * Builds preparation views for upcoming events that can be connected to a
     * project. Events with no matching project are skipped (no fake prep).
     */
    fun preparationsFor(
        events: List<CalendarEventModel>,
        projects: List<ProjectEntity>,
        tasks: List<TaskEntity>,
        nowMillis: Long,
    ): List<EventPreparation> =
        events
            .filter { it.beginMillis >= nowMillis }
            .mapNotNull { event ->
                val project = matchProject(event, projects, tasks) ?: return@mapNotNull null
                buildPreparation(event, project, tasks, nowMillis)
            }
            .sortedBy { it.event.beginMillis }

    private fun buildPreparation(
        event: CalendarEventModel,
        project: ProjectEntity,
        tasks: List<TaskEntity>,
        nowMillis: Long,
    ): EventPreparation {
        val projectTasks = tasks.filter { it.projectId == project.id }
        // Prep work = tasks due before the event (or within its day), excluding
        // the task that *is* the event itself.
        val prepCandidates = projectTasks.filter { task ->
            val isEventItself =
                task.title.equals(event.title, ignoreCase = true) &&
                    task.deadlineEpochMillis != null &&
                    Math.abs(task.deadlineEpochMillis - event.beginMillis) <= 2 * 60 * 60 * 1000L
            !isEventItself &&
                task.deadlineEpochMillis != null &&
                task.deadlineEpochMillis <= event.beginMillis &&
                task.deadlineEpochMillis >= nowMillis - STALE_WINDOW
        }

        val openPrep = prepCandidates.filter { it.status == TaskStatus.ACTIVE.name }
            .sortedBy { it.deadlineEpochMillis ?: Long.MAX_VALUE }
        val donePrep = prepCandidates.filter { it.status == TaskStatus.DONE.name }

        val readiness = if (prepCandidates.isEmpty()) {
            0
        } else {
            (donePrep.size * 100) / prepCandidates.size
        }

        return EventPreparation(
            event = event,
            projectId = project.id,
            projectName = project.name,
            prepTasks = openPrep,
            donePrepCount = donePrep.size,
            readinessPercent = readiness,
            nextPrepTask = openPrep.firstOrNull(),
        )
    }

    private companion object {
        const val STALE_WINDOW = 24 * 60 * 60 * 1000L // ignore prep tasks overdue > 1 day
    }
}
