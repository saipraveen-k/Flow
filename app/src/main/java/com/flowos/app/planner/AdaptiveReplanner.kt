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
        val overrunTaskIndex = 0

        val afterItems = if (criticalAlert.type == FrictionType.ESTIMATION_ERROR) {
            val shiftMs = 30 * 60 * 1000L
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
            reason = criticalAlert.description,
            beforeItems = beforeItems,
            afterItems = afterItems,
            changes = listOf("Shift dependent tasks", "Protect outcome deadline")
        )
    }
}
