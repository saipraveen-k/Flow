package com.flowos.app.planner

import com.flowos.app.data.local.TaskEntity
import com.flowos.app.domain.model.CalendarEventModel
import com.flowos.app.pulse.ThreadOrdering
import com.flowos.app.pulse.TaskDependencyEdge
import java.time.Instant
import java.time.ZoneId
import java.time.LocalDateTime

/**
 * High-intelligence time-aware scheduler.
 * Merges calendar events, task durations, and critical path dependencies
 * into a realistic, adaptive execution plan.
 */
object AdaptivePlanner {

    data class ScheduledItem(
        val taskId: String?,
        val eventId: Long?,
        val title: String,
        val startMillis: Long,
        val endMillis: Long,
        val isEvent: Boolean
    )

    data class Plan(
        val items: List<ScheduledItem>,
        val capacityRemainingMinutes: Int,
        val deadlineRisk: Boolean = false
    )

    fun createPlan(
        tasks: List<TaskEntity>,
        dependencies: List<TaskDependencyEdge>,
        calendarEvents: List<CalendarEventModel>,
        nowMillis: Long,
        planHorizonHours: Int = 24
    ): Plan {
        val zone = ZoneId.systemDefault()
        val horizonEnd = nowMillis + planHorizonHours * 60 * 60 * 1000L
        
        // 1. Sort tasks by dependency first
        val orderedTasks = ThreadOrdering.order(tasks, dependencies)
            .filter { it.completedAt == null }

        val items = mutableListOf<ScheduledItem>()
        
        // 2. Add calendar events as hard constraints
        calendarEvents.filter { it.beginMillis in nowMillis..horizonEnd }
            .forEach { event ->
                items.add(ScheduledItem(
                    taskId = null,
                    eventId = event.eventId,
                    title = event.title,
                    startMillis = event.beginMillis,
                    endMillis = event.endMillis,
                    isEvent = true
                ))
            }

        // 3. Fill available slots with tasks
        var currentTime = nowMillis
        orderedTasks.forEach { task ->
            val durationMs = task.estimatedDurationMinutes * 60 * 1000L
            
            // Find next available slot that doesn't conflict with events
            currentTime = findNextAvailableSlot(currentTime, durationMs, items)
            
            if (currentTime + durationMs <= horizonEnd) {
                items.add(ScheduledItem(
                    taskId = task.id,
                    eventId = null,
                    title = task.title,
                    startMillis = currentTime,
                    endMillis = currentTime + durationMs,
                    isEvent = false
                ))
                currentTime += durationMs
            }
        }

        val sortedItems = items.sortedBy { it.startMillis }
        val totalWorkTime = tasks.sumOf { it.estimatedDurationMinutes }
        val availableTime = planHorizonHours * 60
        
        return Plan(
            items = sortedItems,
            capacityRemainingMinutes = availableTime - totalWorkTime,
            deadlineRisk = checkDeadlineRisk(sortedItems, tasks)
        )
    }

    private fun findNextAvailableSlot(
        start: Long,
        durationMs: Long,
        scheduled: List<ScheduledItem>
    ): Long {
        var candidate = start
        val sorted = scheduled.sortedBy { it.startMillis }
        
        for (item in sorted) {
            if (candidate + durationMs <= item.startMillis) {
                return candidate
            }
            if (candidate < item.endMillis) {
                candidate = item.endMillis
            }
        }
        return candidate
    }

    private fun checkDeadlineRisk(planned: List<ScheduledItem>, tasks: List<TaskEntity>): Boolean {
        val taskDeadlines = tasks.associate { it.id to it.deadlineEpochMillis }
        return planned.any { item ->
            val deadline = taskDeadlines[item.taskId ?: ""]
            deadline != null && item.endMillis > deadline
        }
    }
}
