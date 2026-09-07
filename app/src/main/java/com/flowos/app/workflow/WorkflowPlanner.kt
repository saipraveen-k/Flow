package com.flowos.app.workflow

import android.util.Log
import com.flowos.app.domain.model.AIAnalysisResult
import com.flowos.app.domain.model.ExtractedDependency
import com.flowos.app.domain.model.ExtractedTask
import com.flowos.app.domain.model.Priority
import com.flowos.app.domain.model.WorkflowPlan
import com.flowos.app.domain.model.WorkflowStepPlan

/**
 * Orders extracted tasks into an executable workflow using topological sort.
 * Deterministic: dependency first, then deadline, then priority.
 */
object WorkflowPlanner {

    private const val TAG = "WorkflowPlanner"

    fun plan(analysis: AIAnalysisResult, resolvedProjectName: String? = null): WorkflowPlan {
        val tasks = analysis.tasks
        if (tasks.isEmpty()) {
            return WorkflowPlan(title = "Untitled workflow", steps = emptyList(), deadlineCount = 0)
        }

        val orderedIndices = topologicalOrder(tasks, analysis.dependencies)

        val steps = orderedIndices.mapIndexed { position, taskIndex ->
            val task = tasks[taskIndex]
            WorkflowStepPlan(
                order = position + 1,
                title = task.title,
                deadlineLabel = task.deadlineLabel,
                person = task.person,
                priority = task.priority,
            )
        }

        val title = when {
            resolvedProjectName != null -> resolvedProjectName
            analysis.project != null -> analysis.project
            analysis.summary.isNotBlank() -> analysis.summary
            else -> "Generated Workflow"
        }

        return WorkflowPlan(
            title = title,
            steps = steps,
            deadlineCount = tasks.count { it.deadlineEpochMillis != null },
        )
    }

    /**
     * Kahn's algorithm for topological sorting.
     * Tie-breaking: earliest deadline first, then highest priority.
     */
    private fun topologicalOrder(
        tasks: List<ExtractedTask>,
        dependencies: List<ExtractedDependency>,
    ): List<Int> {
        val n = tasks.size
        val inDegree = IntArray(n)
        val adjacency = List(n) { mutableListOf<Int>() }
        
        dependencies.forEach { dep ->
            if (dep.fromIndex in 0 until n && dep.toIndex in 0 until n) {
                adjacency[dep.fromIndex].add(dep.toIndex)
                inDegree[dep.toIndex]++
            }
        }

        val ready = tasks.indices.filter { inDegree[it] == 0 }
            .sortedBy { tieBreakValue(it, tasks) }
            .toMutableList()
            
        val order = mutableListOf<Int>()

        while (ready.isNotEmpty()) {
            val current = ready.removeAt(0)
            order.add(current)
            
            adjacency[current].forEach { next ->
                inDegree[next]--
                if (inDegree[next] == 0) {
                    ready.add(next)
                }
            }
            // Maintain deterministic order even within steps
            ready.sortBy { tieBreakValue(it, tasks) }
        }

        // Cycle detection & Fallback
        if (order.size < n) {
            tasks.indices.forEach { if (it !in order) order.add(it) }
        }

        return order
    }

    private fun tieBreakValue(index: Int, tasks: List<ExtractedTask>): Long {
        val task = tasks[index]
        val pWeight = when (task.priority) {
            Priority.HIGH -> 0L
            Priority.MEDIUM -> 1_000_000_000L
            Priority.LOW -> 2_000_000_000L
        }
        // Deadline is primary tie-breaker, then priority
        return (task.deadlineEpochMillis ?: (Long.MAX_VALUE / 2)) + pWeight
    }
}
