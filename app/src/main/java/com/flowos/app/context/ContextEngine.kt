package com.flowos.app.context

import com.flowos.app.data.repository.FlowOSRepository
import com.flowos.app.domain.model.AIAnalysisResult
import com.flowos.app.domain.model.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Connects captures to existing projects, people and open work so nothing is
 * stored as an isolated task. When a capture mentions the active project or a
 * known collaborator, new tasks attach to that context.
 */
class ContextEngine(
    private val repository: FlowOSRepository,
) {

    /**
     * Resolve the project a capture belongs to.
     * Order: explicit project mention → active demo project → null.
     */
    suspend fun resolveProject(analysis: AIAnalysisResult): String? =
        withContext(Dispatchers.IO) {
            analysis.project?.let { mentioned ->
                val match = repository.observeProjectsOnce()
                    .firstOrNull { it.name.equals(mentioned, ignoreCase = true) }
                match?.id
            } ?: repository.getActiveProjectId()
        }

    /**
     * Attach a resolved project to every task missing one and return the
     * updated task list, ready for planning and persistence.
     */
    suspend fun connectContext(
        analysis: AIAnalysisResult,
    ): AIAnalysisResult = withContext(Dispatchers.IO) {
        val projectId = resolveProject(analysis)
        if (projectId == null) return@withContext analysis

        val project = repository.getProject(projectId) ?: return@withContext analysis
        analysis.copy(
            tasks = analysis.tasks.map { task ->
                if (task.project == null) task.copy(project = project.name) else task
            },
            people = (analysis.people + rememberPeople(analysis)).distinct(),
        )
    }

    /** Persists newly seen collaborators and returns them. */
    private suspend fun rememberPeople(analysis: AIAnalysisResult): List<String> {
        analysis.people.forEach { repository.rememberPerson(it) }
        return analysis.people
    }

    /** Suggests a priority bump when a deadline is within the next 12 hours. */
    fun adjustPriorityForUrgency(
        priority: Priority,
        deadlineEpochMillis: Long?,
        nowMillis: Long = System.currentTimeMillis(),
    ): Priority {
        val deadline = deadlineEpochMillis ?: return priority
        val withinTwelveHours = deadline - nowMillis <= 12 * 60 * 60 * 1000L
        return if (withinTwelveHours && priority == Priority.MEDIUM) {
            Priority.HIGH
        } else {
            priority
        }
    }
}
