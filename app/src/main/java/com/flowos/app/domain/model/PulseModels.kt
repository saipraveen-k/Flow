package com.flowos.app.domain.model

import com.flowos.app.data.local.TaskEntity

/** A single event read from the device calendar (system provider, not ours). */
data class CalendarEventModel(
    val eventId: Long,
    val title: String,
    val beginMillis: Long,
    val endMillis: Long,
    val location: String? = null,
    val description: String? = null,
    val allDay: Boolean = false,
)

/** One concrete suggested action, derived from real dependencies. */
data class NextBestAction(
    val taskId: String,
    val title: String,
    /** Honest explanation derived from dependency/deadline data. */
    val reason: String,
    val priority: Priority,
    val deadlineLabel: String?,
    val deadlineEpochMillis: Long?,
    val projectName: String?,
    /** Titles of open tasks this action unblocks. */
    val unlocksTaskTitles: List<String>,
)

/**
 * The most relevant active work thread. Everything here is derived from
 * persisted tasks, dependencies and (when permitted) calendar events —
 * never hardcoded to the demo scenario.
 */
data class WorkThread(
    val projectId: String,
    val projectName: String,
    val completionPercentage: Int,
    val openTasks: List<TaskEntity>,
    val blockedTasks: List<TaskEntity>,
    val upcomingDeadlines: List<TaskEntity>,
    val relatedPeople: List<String>,
    val relatedFiles: List<String>,
    val calendarEvents: List<CalendarEventModel>,
    /** Deterministic relevance score used to pick the active thread. */
    val score: Int,
)

/**
 * FlowPulse output: the current state of the user's most important work.
 * Flat accessors delegate to [activeThread] so UI reads the fields from the
 * original spec (completionPercentage, openTasks, blockedTasks, ...) directly.
 */
data class WorkState(
    val activeThread: WorkThread?,
    val nextBestAction: NextBestAction?,
    val reason: String?,
) {
    val activeProject: WorkThread? get() = activeThread
    val completionPercentage: Int get() = activeThread?.completionPercentage ?: 0
    val openTasks: List<TaskEntity> get() = activeThread?.openTasks ?: emptyList()
    val blockedTasks: List<TaskEntity> get() = activeThread?.blockedTasks ?: emptyList()
    val upcomingDeadlines: List<TaskEntity> get() = activeThread?.upcomingDeadlines ?: emptyList()
    val relatedPeople: List<String> get() = activeThread?.relatedPeople ?: emptyList()
    val relatedFiles: List<String> get() = activeThread?.relatedFiles ?: emptyList()
    val calendarEvents: List<CalendarEventModel> get() = activeThread?.calendarEvents ?: emptyList()
    val projectName: String? get() = activeThread?.projectName
    val projectId: String? get() = activeThread?.projectId
}

/**
 * A calendar event joined with the preparation work FlowOS can see for it.
 * Readiness is derived from real prep-task completion, not invented.
 */
data class EventPreparation(
    val event: CalendarEventModel,
    val projectId: String?,
    val projectName: String?,
    /** Open preparation tasks, ordered by deadline. */
    val prepTasks: List<TaskEntity>,
    val donePrepCount: Int,
    val readinessPercent: Int,
    val nextPrepTask: TaskEntity?,
) {
    val hasPreparation: Boolean get() = prepTasks.isNotEmpty() || donePrepCount > 0
}
