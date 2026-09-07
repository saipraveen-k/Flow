package com.flowos.app.pulse

import com.flowos.app.data.local.TaskEntity

/**
 * Orders the open tasks of a work thread so dependencies come first.
 * Kahn's topological sort with deterministic tie-breaks:
 * earliest deadline, then workflow orderIndex, then task id.
 */
object ThreadOrdering {

    fun order(tasks: List<TaskEntity>, edges: List<TaskDependencyEdge>): List<TaskEntity> {
        if (tasks.isEmpty()) return tasks
        val ids = tasks.map { it.id }.toSet()
        val byId = tasks.associateBy { it.id }

        val inDegree = HashMap<String, Int>(ids.size)
        val dependents = HashMap<String, MutableList<String>>()
        tasks.forEach { inDegree[it.id] = 0 }
        edges.forEach { edge ->
            if (edge.fromTaskId in ids && edge.toTaskId in ids) {
                inDegree[edge.toTaskId] = (inDegree[edge.toTaskId] ?: 0) + 1
                dependents.getOrPut(edge.fromTaskId) { mutableListOf() }.add(edge.toTaskId)
            }
        }

        fun sortKey(task: TaskEntity): Long {
            val deadline = task.deadlineEpochMillis
                ?: return Long.MAX_VALUE / 2 - task.orderIndex // no deadline: last, stable
            // Real epoch millis (~1.7e12) * 100 stays far below Long overflow.
            return deadline * 100 + task.orderIndex
        }

        val ready = tasks.filter { (inDegree[it.id] ?: 0) == 0 }.sortedBy { sortKey(it) }.toMutableList()
        val ordered = mutableListOf<TaskEntity>()
        while (ready.isNotEmpty()) {
            val current = ready.removeAt(0)
            ordered += current
            (dependents[current.id] ?: emptyList()).forEach { dependentId ->
                val remaining = (inDegree[dependentId] ?: 0) - 1
                inDegree[dependentId] = remaining
                if (remaining == 0) {
                    byId[dependentId]?.let { ready.add(it) }
                }
            }
            ready.sortBy { sortKey(it) }
        }
        // Cycles: append leftovers deterministically instead of dropping them.
        if (ordered.size < tasks.size) {
            val orderedIds = ordered.map { it.id }.toSet()
            tasks.filter { it.id !in orderedIds }
                .sortedBy { sortKey(it) }
                .forEach { ordered += it }
        }
        return ordered
    }
}
