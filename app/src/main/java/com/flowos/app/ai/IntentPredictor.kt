package com.flowos.app.ai

import com.flowos.app.domain.model.DetectedIntent
import com.flowos.app.domain.model.IntentCategory

/**
 * Deterministic intent prediction from plain keyword heuristics.
 * No model, no network, no learning: identical input always yields identical
 * output, and the confidence reflects match strength — nothing more.
 */
object IntentPredictor {

    private val REVIEW_WORDS = listOf("review", "presentation", "demo", "standup")
    private val MEETING_WORDS = listOf("meeting", "call", "interview", "sync")
    private val SEND_WORDS = listOf("send", "share", "email", "forward", "submit")
    private val DOCUMENT_WORDS = listOf("document", "file", "diagram", "report", "deck", "pdf", "assignment")
    private val FINISH_WORDS = listOf("finish", "complete", "finalize", "finalise", "wrap up")
    private val FOLLOW_UP_WORDS = listOf("follow up", "check on", "ping", "remind", "remind about")
    private val PREPARE_WORDS = listOf("prepare", "get ready", "before the", "for the")

    /**
     * Produces a [DetectedIntent] from [text]. [projectName] and [deadline]
     * (epoch millis) enrich the prediction when the calling engine already
     * resolved them.
     */
    fun predict(
        text: String,
        projectName: String? = null,
        deadlineEpochMillis: Long? = null,
    ): DetectedIntent {
        val lower = text.lowercase()

        val category = when {
            hasAny(lower, REVIEW_WORDS) && hasAny(lower, PREPARE_WORDS + listOf("for", "before")) -> IntentCategory.PREPARE_REVIEW
            hasAny(lower, listOf("assignment", "homework")) && hasAny(lower, SEND_WORDS + listOf("submit")) -> IntentCategory.SUBMIT_ASSIGNMENT
            hasAny(lower, SEND_WORDS) && hasAny(lower, DOCUMENT_WORDS) -> IntentCategory.SEND_DOCUMENT
            hasAny(lower, MEETING_WORDS) && hasAny(lower, PREPARE_WORDS) -> IntentCategory.PREPARE_MEETING
            hasAny(lower, FINISH_WORDS) && hasAny(lower, listOf("project", "architecture", "app", "module", "feature")) -> IntentCategory.FINISH_PROJECT
            hasAny(lower, MEETING_WORDS) -> IntentCategory.PREPARE_MEETING
            hasAny(lower, FOLLOW_UP_WORDS) -> IntentCategory.FOLLOW_UP
            hasAny(lower, FINISH_WORDS) -> IntentCategory.FINISH_PROJECT
            hasAny(lower, SEND_WORDS) -> IntentCategory.SEND_DOCUMENT
            else -> IntentCategory.GENERAL_TASK
        }

        val strongMatch = when (category) {
            IntentCategory.GENERAL_TASK, IntentCategory.UNKNOWN -> false
            else -> true
        }
        val confidence = when {
            !strongMatch -> 0.35
            // A specific action + object keyword pair (e.g. "send" + "document") is a strong signal.
            hasAny(lower, SEND_WORDS) && hasAny(lower, DOCUMENT_WORDS) -> 0.85
            hasAny(lower, MEETING_WORDS) || hasAny(lower, REVIEW_WORDS) -> 0.8
            else -> 0.65
        }

        return DetectedIntent(
            category = category,
            label = labelFor(category, projectName),
            confidence = confidence,
            suggestedActions = suggestedActionsFor(category),
            relatedProject = projectName,
            relatedDeadlineEpochMillis = deadlineEpochMillis,
        )
    }

    private fun hasAny(text: String, words: List<String>): Boolean =
        words.any { text.contains(it) }

    private fun labelFor(category: IntentCategory, projectName: String?): String = when (category) {
        IntentCategory.PREPARE_REVIEW -> "prepare for review"
        IntentCategory.SUBMIT_ASSIGNMENT -> "submit assignment"
        IntentCategory.FINISH_PROJECT -> projectName?.let { "finish $it".lowercase() } ?: "finish project"
        IntentCategory.SEND_DOCUMENT -> "send document"
        IntentCategory.PREPARE_MEETING -> "prepare meeting"
        IntentCategory.FOLLOW_UP -> "follow up with person"
        IntentCategory.GENERAL_TASK -> "handle task"
        IntentCategory.UNKNOWN -> "unclear intent"
    }

    private fun suggestedActionsFor(category: IntentCategory): List<String> = when (category) {
        IntentCategory.PREPARE_REVIEW -> listOf("Prepare notes", "Update documents", "Confirm attendees")
        IntentCategory.SUBMIT_ASSIGNMENT -> listOf("Finish the work", "Review submission rules", "Submit before deadline")
        IntentCategory.SEND_DOCUMENT -> listOf("Attach the document", "Pick recipient", "Send before deadline")
        IntentCategory.PREPARE_MEETING -> listOf("Draft agenda", "Prepare notes", "Confirm time")
        IntentCategory.FINISH_PROJECT -> listOf("Finish remaining work", "Review the result", "Notify the team")
        IntentCategory.FOLLOW_UP -> listOf("Draft the message", "Choose the channel", "Follow up today")
        IntentCategory.GENERAL_TASK -> listOf("Clarify the goal", "Pick a deadline", "Start the task")
        IntentCategory.UNKNOWN -> listOf("Review the capture")
    }
}
