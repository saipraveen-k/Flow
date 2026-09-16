package com.flowos.app.pulse

import com.flowos.app.data.local.TaskEntity
import com.flowos.app.domain.model.Priority
import com.flowos.app.domain.model.TaskStatus

/**
 * Represents the intelligent dependency graph for an Outcome.
 * Provides insights into blockers, critical path, and downstream impact.
 */
class WorkGraph(
    val tasks: List<TaskEntity>,
    val edges: List<TaskDependencyEdge>
) {
    private val taskMap = tasks.associateBy { it.id }
    private val adj = tasks.associate { it.id to mutableListOf<String>() }
    private val revAdj = tasks.associate { it.id to mutableListOf<String>() }

    init {
        edges.forEach { edge ->
            if (adj.containsKey(edge.fromTaskId) && adj.containsKey(edge.toTaskId)) {
                adj[edge.fromTaskId]?.add(edge.toTaskId)
                revAdj[edge.toTaskId]?.add(edge.fromTaskId)
            }
        }
    }

    /** Returns tasks that are currently blocking the most downstream work. */
    fun getCriticalBlockers(): List<TaskEntity> {
        val openTasks = tasks.filter { it.status == TaskStatus.ACTIVE.name }
        return openTasks.filter { id ->
            (revAdj[id.id] ?: emptyList()).all { blockerId ->
                taskMap[blockerId]?.status == TaskStatus.DONE.name
            }
        }.sortedByDescending { countDownstream(it.id) }
    }

    /** Returns the number of tasks that directly or indirectly depend on this task. */
    fun countDownstream(taskId: String): Int {
        val visited = mutableSetOf<String>()
        fun dfs(id: String) {
            visited.add(id)
            adj[id]?.forEach { if (it !in visited) dfs(it) }
        }
        dfs(taskId)
        return visited.size - 1 // Exclude self
    }

    /** Returns true if the task is on the critical path to the final deadline. */
    fun isOnCriticalPath(taskId: String): Boolean {
        // Simple heuristic: if it has high downstream impact or high priority
        return countDownstream(taskId) > 0 || taskMap[taskId]?.priority == Priority.HIGH.name
    }

    /** Returns the tasks that are directly blocked by this task. */
    fun getDirectDependents(taskId: String): List<TaskEntity> {
        return adj[taskId]?.mapNotNull { taskMap[it] } ?: emptyList()
    }
}
