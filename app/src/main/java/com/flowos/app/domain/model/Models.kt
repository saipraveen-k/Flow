package com.flowos.app.domain.model

/** A raw user capture before understanding. */
data class CaptureDraft(
    val sourceType: SourceType,
    val text: String,
    /** Local file path (image/document) when the capture carries an attachment. */
    val attachmentPath: String? = null,
)

/** One actionable item extracted by the understanding engine. */
data class ExtractedTask(
    val title: String,
    val description: String = "",
    val priority: Priority = Priority.MEDIUM,
    /** Epoch millis; null when no deadline could be resolved. */
    val deadlineEpochMillis: Long? = null,
    /** Human readable form, e.g. "Tonight · 8:00 PM". */
    val deadlineLabel: String? = null,
    val person: String? = null,
    val project: String? = null,
    val requiresSharing: Boolean = false,
    val requiresFile: Boolean = false,
)

/** A deadline mentioned in the capture (may be event-like rather than task bound). */
data class ExtractedDeadline(
    val label: String,
    val epochMillis: Long,
    val isEvent: Boolean,
)

/** Directed dependency between two extracted tasks (indices into the task list). */
data class ExtractedDependency(
    val fromIndex: Int,
    val toIndex: Int,
    val reason: String,
)

/** Coarse intent category the heuristic predictor can resolve. */
enum class IntentCategory {
    PREPARE_REVIEW,
    SUBMIT_ASSIGNMENT,
    FINISH_PROJECT,
    SEND_DOCUMENT,
    PREPARE_MEETING,
    FOLLOW_UP,
    GENERAL_TASK,
    UNKNOWN,
}

/**
 * Intent *prediction* produced by deterministic heuristics — keyword scoring,
 * not machine learning. The confidence value reflects match strength honestly.
 */
data class DetectedIntent(
    val category: IntentCategory,
    /** Short human phrase, e.g. "prepare for review". */
    val label: String,
    val confidence: Double,
    val suggestedActions: List<String>,
    val relatedProject: String? = null,
    val relatedDeadlineEpochMillis: Long? = null,
)

/** Structured result of understanding a capture. Application logic reads ONLY this. */
data class AIAnalysisResult(
    val summary: String,
    val intent: IntentType,
    val priority: Priority,
    val people: List<String>,
    val deadlines: List<ExtractedDeadline>,
    val tasks: List<ExtractedTask>,
    val dependencies: List<ExtractedDependency>,
    val project: String?,
    val confidence: Double,
    val sourceType: SourceType,
    /** Richer intent prediction; null for engines that don't produce one. */
    val detectedIntent: DetectedIntent? = null,
)

/** A fully processed capture ready for user confirmation. */
data class ProcessedCapture(
    val draft: CaptureDraft,
    val analysis: AIAnalysisResult,
    val processingMode: String,
)

/** Ordered workflow plan derived from an analysis. */
data class WorkflowPlan(
    val title: String,
    val steps: List<WorkflowStepPlan>,
    val deadlineCount: Int,
)

/** One node of a workflow plan, in execution order. */
data class WorkflowStepPlan(
    val order: Int,
    val title: String,
    val deadlineLabel: String?,
    val person: String?,
    val priority: Priority,
)
