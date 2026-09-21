package com.flowos.app.pulse

import com.flowos.app.data.local.TaskEntity
import com.flowos.app.planner.RoutineConflict

enum class FrictionType {
    OVERRUN,
    BLOCKED_TASK,
    CALENDAR_COLLISION,
    INSUFFICIENT_CAPACITY,
    DEADLINE_RISK,
    ROUTINE_CONFLICT,
    FITNESS_CONFLICT,
    PC_UNAVAILABLE,
    TRANSFER_FAILURE,
    ESTIMATION_ERROR
}

data class FrictionAlertAction(
    val label: String,
    val actionKey: String
)

data class FrictionAlert(
    val id: String,
    val type: FrictionType,
    val title: String,
    val description: String,
    val actions: List<FrictionAlertAction>
)

class FrictionRadar {

    fun analyzeFriction(
        openTasks: List<TaskEntity>,
        routineConflicts: List<RoutineConflict>,
        isPcConnected: Boolean,
        availableCapacityMinutes: Int
    ): List<FrictionAlert> {
        val alerts = mutableListOf<FrictionAlert>()

        // 1. PC Unavailable
        val requiresPcTask = openTasks.firstOrNull { task ->
            task.description.contains("PC", ignoreCase = true) ||
                    task.description.contains("laptop", ignoreCase = true) ||
                    task.title.contains("PPT", ignoreCase = true) ||
                    task.title.contains("code", ignoreCase = true)
        }
        if (!isPcConnected && requiresPcTask != null) {
            alerts.add(
                FrictionAlert(
                    id = "friction_pc_unavailable",
                    type = FrictionType.PC_UNAVAILABLE,
                    title = "PC Unavailable",
                    description = "Task '${requiresPcTask.title}' requires desktop execution, but no companion PC is connected.",
                    actions = listOf(
                        FrictionAlertAction("Connect PC", "ACTION_CONNECT_PC"),
                        FrictionAlertAction("Work on Phone", "ACTION_WORK_ON_PHONE"),
                        FrictionAlertAction("Replan Task", "ACTION_REPLAN")
                    )
                )
            )
        }

        // 2. Insufficient Capacity
        val totalEstimatedMinutes = openTasks.sumOf { it.estimatedDurationMinutes }
        if (totalEstimatedMinutes > availableCapacityMinutes && availableCapacityMinutes > 0) {
            alerts.add(
                FrictionAlert(
                    id = "friction_capacity",
                    type = FrictionType.INSUFFICIENT_CAPACITY,
                    title = "Schedule Over Capacity",
                    description = "Your open tasks total ${totalEstimatedMinutes}m, but available focus time is only ${availableCapacityMinutes}m.",
                    actions = listOf(
                        FrictionAlertAction("Move Flexible Task", "ACTION_MOVE_TASK"),
                        FrictionAlertAction("Shorten Focus Block", "ACTION_SHORTEN_BLOCK"),
                        FrictionAlertAction("Replan Day", "ACTION_REPLAN_DAY")
                    )
                )
            )
        }

        // 3. Routine Conflict
        for (conflict in routineConflicts) {
            alerts.add(
                FrictionAlert(
                    id = "friction_routine_${conflict.routineBlockTitle.hashCode()}",
                    type = FrictionType.ROUTINE_CONFLICT,
                    title = "Routine Conflict: ${conflict.routineBlockTitle}",
                    description = "Collides with calendar event '${conflict.calendarEventTitle}'.",
                    actions = listOf(
                        FrictionAlertAction("Adjust Routine", "ACTION_ADJUST_ROUTINE"),
                        FrictionAlertAction("Keep Calendar", "ACTION_KEEP_CALENDAR"),
                        FrictionAlertAction("Propose Replan", "ACTION_REPLAN")
                    )
                )
            )
        }

        // 4. Overrun & Estimation Error
        val overrunTask = openTasks.firstOrNull { it.actualDurationMinutes > (it.estimatedDurationMinutes * 1.5).toInt() }
        if (overrunTask != null) {
            alerts.add(
                FrictionAlert(
                    id = "friction_overrun_${overrunTask.id}",
                    type = FrictionType.ESTIMATION_ERROR,
                    title = "Task Overrun Detected",
                    description = "Task '${overrunTask.title}' exceeded initial estimate of ${overrunTask.estimatedDurationMinutes}m.",
                    actions = listOf(
                        FrictionAlertAction("Update Estimate", "ACTION_UPDATE_ESTIMATE"),
                        FrictionAlertAction("Split Task", "ACTION_SPLIT_TASK")
                    )
                )
            )
        }

        return alerts
    }
}
