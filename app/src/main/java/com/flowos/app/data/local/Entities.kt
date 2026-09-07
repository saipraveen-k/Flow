package com.flowos.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val colorHex: String = "#4F8CFF",
    val progressPercent: Int = 0,
    val createdAt: Long,
)

@Entity(
    tableName = "people",
    indices = [Index(value = ["name"], unique = true)],
)
data class PersonEntity(
    @PrimaryKey val id: String,
    val name: String,
    val firstSeenAt: Long,
)

@Entity(
    tableName = "tasks",
    indices = [Index(value = ["projectId"]), Index(value = ["status"])],
)
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String = "",
    val status: String, // TaskStatus.name
    val priority: String, // Priority.name
    val deadlineEpochMillis: Long? = null,
    val deadlineLabel: String? = null,
    val personName: String? = null,
    val projectId: String? = null,
    val sourceCaptureId: String? = null,
    val orderIndex: Int = 0,
    val createdAt: Long,
    val completedAt: Long? = null,
)

@Entity(tableName = "captures")
data class CaptureEntity(
    @PrimaryKey val id: String,
    val sourceType: String, // SourceType.name
    val rawText: String,
    val attachmentPath: String? = null,
    val summary: String? = null,
    val intent: String? = null, // IntentType.name
    val priority: String? = null, // Priority.name
    val confidence: Double = 0.0,
    val processingMode: String? = null,
    val detectedPeople: String = "", // comma-joined names
    val detectedDeadlines: String = "", // comma-joined labels
    val projectId: String? = null,
    val createdAt: Long,
)

@Entity(
    tableName = "workflows",
    indices = [Index(value = ["captureId"])],
)
data class WorkflowEntity(
    @PrimaryKey val id: String,
    val captureId: String,
    val title: String,
    val status: String, // PENDING | EXECUTED
    val executedAt: Long? = null,
    val createdAt: Long,
)

@Entity(
    tableName = "workflow_steps",
    indices = [Index(value = ["workflowId"]), Index(value = ["taskId"])],
)
data class WorkflowStepEntity(
    @PrimaryKey val id: String,
    val workflowId: String,
    val taskId: String,
    val orderIndex: Int,
    val action: String, // ActionDescriptor.name, e.g. CREATE_REMINDER
    val actionDetail: String = "",
    val completed: Boolean = false,
)

@Entity(
    tableName = "activity_events",
    indices = [Index(value = ["createdAt"])],
)
data class ActivityEventEntity(
    @PrimaryKey val id: String,
    val type: String, // CAPTURE_CREATED | WORKFLOW_GENERATED | TASK_COMPLETED | REMINDER_SCHEDULED
    val title: String,
    val detail: String? = null,
    val createdAt: Long,
)

@Entity(
    tableName = "task_dependencies",
    indices = [Index(value = ["fromTaskId"]), Index(value = ["toTaskId"])],
)
/**
 * Directed edge: [fromTaskId] must be completed before [toTaskId]. Persisted
 * from confirmed analyses so FlowPulse can compute blocked tasks and the Flow
 * graph can render real chains across app restarts.
 */
data class TaskDependencyEntity(
    @PrimaryKey val id: String,
    val fromTaskId: String,
    val toTaskId: String,
    val reason: String = "",
    val createdAt: Long,
)
