package com.flowos.app.pulse

import com.flowos.app.data.local.TaskEntity
import com.flowos.app.domain.model.CalendarEventModel
import com.flowos.app.planner.AdaptivePlanner

/**
 * Detects plan-execution divergence and triggers alerts when critical paths are at risk.
 * Primary differentiator: FlowOS identifies "friction" before it becomes a failure.
 */
object FrictionRadar {

    enum class FrictionType {
        TASK_OVERRUN,
        CALENDAR_COLLISION,
        CAPACITY_SHORTAGE,
        DEADLINE_RISK,
        CRITICAL_PATH_BLOCKED
    }

    data class FrictionAlert(
        val type: FrictionType,
        val message: String,
        val severity: Severity,
        val relatedTaskId: String? = null
    )

    enum class Severity { LOW, MEDIUM, HIGH, CRITICAL }

    fun detect(
        tasks: List<TaskEntity>,
        dependencies: List<TaskDependencyEdge>,
        calendarEvents: List<CalendarEventModel>,
        nowMillis: Long
    ): List<FrictionAlert> {
        val alerts = mutableListOf<FrictionAlert>()
        val plan = AdaptivePlanner.createPlan(tasks, dependencies, calendarEvents, nowMillis)

        // 1. Task Overrun Detection
        tasks.filter { it.startedAt != null && it.completedAt == null }.forEach { task ->
            val elapsedMinutes = (nowMillis - task.startedAt!!) / (60 * 1000L)
            if (elapsedMinutes > task.estimatedDurationMinutes) {
                alerts.add(FrictionAlert(
                    type = FrictionType.TASK_OVERRUN,
                    message = "\"${task.title}\" is ${elapsedMinutes - task.estimatedDurationMinutes} min over estimate.",
                    severity = if (elapsedMinutes > task.estimatedDurationMinutes * 1.5) Severity.HIGH else Severity.MEDIUM,
                    relatedTaskId = task.id
                ))
            }
        }

        // 2. Deadline Risk Detection
        if (plan.deadlineRisk) {
            alerts.add(FrictionAlert(
                type = FrictionType.DEADLINE_RISK,
                message = "Plan suggests one or more tasks will miss their deadline.",
                severity = Severity.CRITICAL
            ))
        }

        // 3. Capacity Shortage
        if (plan.capacityRemainingMinutes < 0) {
            alerts.add(FrictionAlert(
                type = FrictionType.CAPACITY_SHORTAGE,
                message = "Your plan exceeds available time today by ${-plan.capacityRemainingMinutes} min.",
                severity = Severity.HIGH
            ))
        }

        return alerts
    }
}
