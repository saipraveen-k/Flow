package com.flowos.app.action

import com.flowos.app.data.local.TaskEntity
import com.flowos.app.domain.model.EventPreparation
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * A bundle of related SAFE actions the user can approve as one unit.
 * Bundles are never executed implicitly — the UI must present an explicit
 * "APPROVE & EXECUTE" confirmation first. Results are reported honestly.
 */
data class ActionBundle(
    val id: String,
    val title: String,
    val description: String,
    val items: List<ActionDescriptor>,
)

/**
 * Deterministic bundle builder. Pure function of its inputs so it can be
 * unit-tested without Android.
 */
object ActionBundleBuilder {

    /**
     * Builds the "PREPARE FOR EVENT" bundle for a calendar event.
     *  - With existing prep tasks: reminder + shareable prep summary.
     *  - Without prep tasks: a deterministic starter checklist is proposed as
     *    CREATE_TASK items (executed through the local task repository).
     */
    fun buildPreparationBundle(prep: EventPreparation): ActionBundle {
        val formatter = DateTimeFormatter.ofPattern("EEE d MMM · h:mm a", Locale.US)
        val start = Instant.ofEpochMilli(prep.event.beginMillis)
            .atZone(ZoneId.systemDefault())
            .format(formatter)

        val items = mutableListOf<ActionDescriptor>()
        val generatedTasks = prep.prepTasks.isEmpty()

        if (generatedTasks) {
            defaultChecklist(prep.event.title).forEach { title ->
                items += ActionDescriptor(
                    kind = ActionKind.CREATE_TASK,
                    title = title,
                    detail = "New local task",
                )
            }
        } else {
            prep.prepTasks.forEach { task ->
                items += ActionDescriptor(
                    kind = ActionKind.CREATE_REMINDER,
                    title = task.title,
                    detail = "Reminder before ${task.deadlineLabel ?: start}",
                    atMillis = task.deadlineEpochMillis,
                )
            }
        }

        items += ActionDescriptor(
            kind = ActionKind.CREATE_REMINDER,
            title = "${prep.event.title} starts soon",
            detail = "Notification 1 hour before the event",
            atMillis = prep.event.beginMillis - 60 * 60 * 1000L,
        )
        items += ActionDescriptor(
            kind = ActionKind.SHARE_TEXT,
            title = "Share prep summary",
            detail = "Opens the share sheet with a status text",
        )

        val readinessLine = if (prep.hasPreparation) {
            "${prep.readinessPercent}% ready · ${prep.prepTasks.size} open / ${prep.donePrepCount} done"
        } else {
            "No preparation tasks yet — FlowOS can create a starter checklist."
        }

        return ActionBundle(
            id = "bundle_${prep.event.eventId}",
            title = "PREPARE FOR ${prep.event.title.uppercase()}",
            description = "$start · ${prep.projectName ?: "unlinked project"}\n$readinessLine",
            items = items,
        )
    }

    /** Deterministic starter checklist used when an event has no prep tasks. */
    fun defaultChecklist(eventTitle: String): List<String> = listOf(
        "Prepare notes for $eventTitle",
        "Review materials for $eventTitle",
        "Confirm attendees for $eventTitle",
    )
}

/** Convenience: prep summary text shared from the bundle. */
fun prepSummaryText(prep: EventPreparation, prepTasks: List<TaskEntity>): String =
    buildString {
        append("${prep.event.title} — prep status\n")
        if (prepTasks.isEmpty() && prep.donePrepCount == 0) {
            append("No preparation tasks tracked yet.")
        } else {
            prepTasks.forEach { append("• ${it.title} (${it.deadlineLabel ?: "no deadline"})\n") }
            if (prep.donePrepCount > 0) append("✓ ${prep.donePrepCount} already done")
        }
    }
