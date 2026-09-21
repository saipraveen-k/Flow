package com.flowos.app.planner

import com.flowos.app.data.local.TaskEntity
import com.flowos.app.pulse.FrictionAlert
import com.flowos.app.pulse.FrictionRadar
import com.flowos.app.pulse.FrictionType

/**
 * Proposes schedule adaptations when friction is detected.
 * Follows the flow: Detect -> Explain -> Propose -> Review.
 */
object AdaptiveReplanner {

    data class ReplanProposal(
        val reason: String,
        val beforeItems: List<AdaptivePlanner.ScheduledItem>,
        val afterItems: List<AdaptivePlanner.ScheduledItem>,
        val changes: List<String>
    )

    fun propose(
        alerts: List<FrictionAlert>,
        currentPlan: AdaptivePlanner.Plan,
        tasks: List<TaskEntity>
    ): ReplanProposal? {
        if (alerts.isEmpty()) return null

        val criticalAlert = alerts.firstOrNull() ?: return null
        val beforeItems = currentPlan.items

        val afterItems = if (criticalAlert.type == FrictionType.ESTIMATION_ERROR) {
            val overrunTaskId = criticalAlert.id.removePrefix("friction_overrun_")
            val task = tasks.find { it.id == overrunTaskId }
            val shiftMs = if (task != null) {
                (task.actualDurationMinutes - task.estimatedDurationMinutes).coerceAtLeast(0) * 60_000L
            } else 0L

            val targetIndex = beforeItems.indexOfFirst { it.taskId == overrunTaskId }
            if (targetIndex != -1 && shiftMs > 0) {
                beforeItems.mapIndexed { index, item ->
                    if (index == targetIndex) {
                        item.copy(endMillis = item.endMillis + shiftMs)
                    } else if (index > targetIndex) {
                        item.copy(startMillis = item.startMillis + shiftMs, endMillis = item.endMillis + shiftMs)
                    } else {
                        item
                    }
                }
            } else beforeItems
        } else {
            beforeItems
        }

        return ReplanProposal(
            reason = criticalAlert.description,
            beforeItems = beforeItems,
            afterItems = afterItems,
            changes = if (afterItems != beforeItems) listOf("Shift downstream execution", "Account for real-world overrun") else emptyList()
        )
    }
}
