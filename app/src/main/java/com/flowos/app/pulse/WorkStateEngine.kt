package com.flowos.app.pulse

import com.flowos.app.data.local.ProjectEntity
import com.flowos.app.data.local.TaskEntity
import com.flowos.app.domain.model.CalendarEventModel
import com.flowos.app.domain.model.NextBestAction
import com.flowos.app.domain.model.Priority
import com.flowos.app.domain.model.TaskStatus
import com.flowos.app.domain.model.WorkState
import com.flowos.app.domain.model.WorkThread

/**
 * FlowPulse: derives the user's most relevant active work thread from real
 * persisted data (tasks, dependencies, projects) plus optional calendar events.
 *
 * The algorithm is fully deterministic — the same inputs always produce the
 * same thread, action and reason. No randomness, no fake statistics.
 *
 * Thread priority factors (in order):
 *  1. deadline proximity
 *  2. dependency blocking (open tasks waiting on other open tasks)
 *  3. task priority
 *  4. incomplete status
 *  5. project relevance (volume of open work)
 */
class WorkStateEngine(
    private val nextBestActionEngine: NextBestActionEngine = NextBestActionEngine(),
) {

    fun compute(
        projects: List<ProjectEntity>,
        tasks: List<TaskEntity>,
        dependencies: List<TaskDependencyEdge>,
        calendarEvents: List<CalendarEventModel>,
        nowMillis: Long,
    ): WorkState {
        val threads = projects.map { project ->
            buildThread(project, tasks, dependencies, calendarEvents, nowMillis)
        }.filter { it.openTasks.isNotEmpty() || it.completionPercentage in 1..99 }

        val active = threads.filter { it.openTasks.isNotEmpty() }
            .maxByOrNull { it.score }

        return if (active == null) {
            WorkState(activeThread = null, nextBestAction = null, reason = null)
        } else {
            val action = nextBestActionEngine.forTasks(
                openTasks = active.openTasks,
                dependencies = dependencies,
                projectName = active.projectName,
                nowMillis = nowMillis,
            )
            WorkState(
                activeThread = active,
                nextBestAction = action,
                reason = action?.reason,
            )
        }
    }

    private fun buildThread(
        project: ProjectEntity,
        tasks: List<TaskEntity>,
        dependencies: List<TaskDependencyEdge>,
        calendarEvents: List<CalendarEventModel>,
        nowMillis: Long,
    ): WorkThread {
        val projectTasks = tasks.filter { it.projectId == project.id }
        val open = projectTasks.filter { it.status == TaskStatus.ACTIVE.name }
            .sortedWith(
                compareBy(
                    { it.deadlineEpochMillis ?: Long.MAX_VALUE },
                    { it.orderIndex },
                ),
            )
        val done = projectTasks.size - open.size
        val completion = if (projectTasks.isEmpty()) {
            0
        } else {
            (done * 100) / projectTasks.size
        }

        val openIds = open.map { it.id }.toSet()
        val blocked = open.filter { task ->
            dependencies.any { edge -> edge.toTaskId == task.id && edge.fromTaskId in openIds }
        }
        val upcoming = open.filter { task ->
            task.deadlineEpochMillis != null && task.deadlineEpochMillis >= nowMillis
        }

        val score = threadScore(open, blocked, nowMillis)
        return WorkThread(
            projectId = project.id,
            projectName = project.name,
            completionPercentage = completion,
            openTasks = open,
            blockedTasks = blocked,
            upcomingDeadlines = upcoming,
            relatedPeople = open.mapNotNull { it.personName }.distinct(),
            relatedFiles = open.map { it.title }.filter { looksLikeFileWork(it) }.distinct(),
            calendarEvents = calendarEvents.filter { belongsToProject(it, project, projectTasks) },
            score = score,
        )
    }

    private fun threadScore(
        open: List<TaskEntity>,
        blocked: List<TaskEntity>,
        nowMillis: Long,
    ): Int {
        val nearestDeadline = open.mapNotNull { it.deadlineEpochMillis }.minOrNull()
        var score = 0
        // Urgency
        score += when {
            nearestDeadline == null -> 0
            nearestDeadline <= nowMillis + SIX_HOURS -> 50
            nearestDeadline <= nowMillis + DAY -> 35
            nearestDeadline <= nowMillis + THREE_DAYS -> 20
            else -> 10
        }
        // Blocking impact
        score += blocked.size.coerceAtMost(5) * 6
        // Priority
        score += open.count { Priority.from(it.priority) == Priority.HIGH }.coerceAtMost(4) * 5
        // Volume/Complexity
        score += (open.sumOf { it.estimatedDurationMinutes } / 60).coerceAtMost(10)
        return score
    }

    private fun belongsToProject(
        event: CalendarEventModel,
        project: ProjectEntity,
        projectTasks: List<TaskEntity>,
    ): Boolean {
        val titleLower = event.title.lowercase()
        val nameWords = project.name.lowercase()
            .split(Regex("\\s+"))
            .filter { it.length >= 5 }
        if (nameWords.any { titleLower.contains(it) }) return true

        // Fallback: an event starting within 12h of a project deadline belongs
        // to that project's thread.
        return projectTasks.any { task ->
            task.deadlineEpochMillis != null &&
                Math.abs(task.deadlineEpochMillis - event.beginMillis) <= 12 * 60 * 60 * 1000L
        }
    }

    /** Titles that imply a file/document deliverable, used for the FILES node. */
    private fun looksLikeFileWork(title: String): Boolean =
        FILE_WORDS.any { title.lowercase().contains(it) }

    companion object {
        private const val SIX_HOURS = 6 * 60 * 60 * 1000L
        private const val DAY = 24 * 60 * 60 * 1000L
        private const val THREE_DAYS = 3 * DAY

        private val FILE_WORDS = listOf(
            "document", "diagram", "file", "pdf", "deck", "presentation", "report", "slides",
        )
    }
}
