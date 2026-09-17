package com.flowos.app.planner

import com.flowos.app.data.local.TaskEntity
import com.flowos.app.pulse.FrictionRadar

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
        alerts: List<FrictionRadar.FrictionAlert>,
        currentPlan: AdaptivePlanner.Plan,
        tasks: List<TaskEntity>
    ): ReplanProposal? {
        if (alerts.isEmpty()) return null

        val criticalAlert = alerts.maxByOrNull { it.severity } ?: return null
        
        // Simulating the adaptation: shift all items after the overrun task
        val beforeItems = currentPlan.items
        val overrunTaskIndex = beforeItems.indexOfFirst { it.taskId == criticalAlert.relatedTaskId }
        
        val afterItems = if (overrunTaskIndex != -1 && criticalAlert.type == FrictionRadar.FrictionType.TASK_OVERRUN) {
            val shiftMs = 32 * 60 * 1000L // Simulate 32 min overrun for the demo
            beforeItems.mapIndexed { index, item ->
                if (index == overrunTaskIndex) {
                    item.copy(endMillis = item.endMillis + shiftMs)
                } else if (index > overrunTaskIndex) {
                    item.copy(startMillis = item.startMillis + shiftMs, endMillis = item.endMillis + shiftMs)
                } else {
                    item
                }
            }
        } else {
            beforeItems
        }

        return ReplanProposal(
            reason = criticalAlert.message,
            beforeItems = beforeItems,
            afterItems = afterItems,
            changes = listOf("Shift dependent tasks", "Protect outcome deadline")
        )
    }
}
