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
        val oldPlan: AdaptivePlanner.Plan,
        val newPlan: AdaptivePlanner.Plan,
        val changes: List<String>
    )

    fun propose(
        alerts: List<FrictionRadar.FrictionAlert>,
        currentPlan: AdaptivePlanner.Plan,
        tasks: List<TaskEntity>
    ): ReplanProposal? {
        if (alerts.isEmpty()) return null

        val criticalAlert = alerts.maxByOrNull { it.severity } ?: return null
        
        // Simulating replanning logic: in a real implementation, we would
        // adjust task durations or reorder flexible tasks here.
        val adjustedTasks = tasks.map { task ->
            if (task.id == criticalAlert.relatedTaskId && criticalAlert.type == FrictionRadar.FrictionType.TASK_OVERRUN) {
                // Heuristic: if task overran, maybe we should allocate more time or compress others
                task
            } else {
                task
            }
        }

        // For MVP, we just demonstrate the comparison concept.
        return ReplanProposal(
            reason = criticalAlert.message,
            oldPlan = currentPlan,
            newPlan = currentPlan, // Simplified for demo
            changes = listOf("Optimize remaining task durations", "Protect critical path deadline")
        )
    }
}
