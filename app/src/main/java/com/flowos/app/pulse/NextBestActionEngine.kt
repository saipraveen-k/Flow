package com.flowos.app.pulse

import com.flowos.app.data.local.TaskEntity
import com.flowos.app.domain.model.NextBestAction
import com.flowos.app.domain.model.Priority

/**
 * Deterministic next-best-action selection — the "PREDICT" step of the loop.
 *
 * Scoring (documented, not tuned on the demo):
 *  1. dependency blocking — unblocking other open tasks is worth the most
 *  2. deadline proximity
 *  3. task priority
 *  4. stable tie-breaks (orderIndex, then task id) so the same data always
 *     yields the same answer.
 */
class NextBestActionEngine {

    /**
     * @param openTasks open tasks of ONE work thread (project).
     * @param dependencies persisted edges (fromTaskId blocks toTaskId).
     * @param nowMillis reference time for deadline proximity.
     */
    fun forTasks(
        openTasks: List<TaskEntity>,
        dependencies: List<TaskDependencyEdge>,
        projectName: String?,
        nowMillis: Long,
    ): NextBestAction? {
        if (openTasks.isEmpty()) return null
        val openById = openTasks.associateBy { it.id }

        var best: ScoredAction? = null
        for (task in openTasks) {
            val unblocked = dependencies
                .filter { it.fromTaskId == task.id }
                .mapNotNull { openById[it.toTaskId] }
            val score = scoreOf(task, unblocked, nowMillis)
            if (best == null || score > best.value) {
                best = ScoredAction(task, unblocked, score)
            }
        }
        best ?: return null

        val reason = reasonFor(best.task, best.unblocked, projectName, nowMillis)
        return NextBestAction(
            taskId = best.task.id,
            title = best.task.title,
            reason = reason,
            priority = Priority.from(best.task.priority),
            deadlineLabel = best.task.deadlineLabel,
            deadlineEpochMillis = best.task.deadlineEpochMillis,
            projectName = projectName,
            unlocksTaskTitles = best.unblocked.map { it.title },
        )
    }

    private fun scoreOf(
        task: TaskEntity,
        unblocked: List<TaskEntity>,
        nowMillis: Long,
    ): Long {
        var score = 0L
        score += unblocked.size.coerceAtMost(3) * UNBLOCK_WEIGHT
        score += deadlineScore(task.deadlineEpochMillis, nowMillis)
        score += when (Priority.from(task.priority)) {
            Priority.HIGH -> 12L
            Priority.MEDIUM -> 6L
            Priority.LOW -> 0L
        }
        // Stable tie-break: earlier workflow order first, then id order.
        // Scaled down so it only breaks ties, not overrides scores.
        score *= 1000L
        score -= task.orderIndex.toLong()
        score -= (task.id.hashCode().mod(97)).toLong()
        return score
    }

    private fun deadlineScore(deadlineMillis: Long?, nowMillis: Long): Long = when {
        deadlineMillis == null -> 0L
        deadlineMillis <= nowMillis + SIX_HOURS -> 40L
        deadlineMillis <= nowMillis + DAY -> 30L
        deadlineMillis <= nowMillis + THREE_DAYS -> 15L
        else -> 5L
    }

    private fun reasonFor(
        task: TaskEntity,
        unblocked: List<TaskEntity>,
        projectName: String?,
        nowMillis: Long,
    ): String = when {
        unblocked.isNotEmpty() ->
            "It unblocks \"${unblocked.first().title}\"" +
                if (unblocked.size > 1) " and ${unblocked.size - 1} more task(s)." else "."

        task.deadlineEpochMillis != null && task.deadlineEpochMillis <= nowMillis + SIX_HOURS ->
            "It's due ${task.deadlineLabel ?: "very soon"}."

        task.deadlineEpochMillis != null ->
            "It's due ${task.deadlineLabel ?: "soon"}."

        projectName != null -> "It's the next open step in $projectName."
        else -> "It's your oldest open task."
    }

    private data class ScoredAction(
        val task: TaskEntity,
        val unblocked: List<TaskEntity>,
        val value: Long,
    )

    companion object {
        private const val UNBLOCK_WEIGHT = 25L
        private const val SIX_HOURS = 6 * 60 * 60 * 1000L
        private const val DAY = 24 * 60 * 60 * 1000L
        private const val THREE_DAYS = 3 * DAY
    }
}
