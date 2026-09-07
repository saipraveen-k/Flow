package com.flowos.app.action

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.flowos.app.domain.model.ExtractedTask
import com.flowos.app.notifications.FlowOSNotifier
import java.io.File

/** What an execution step wants to do on the device. */
enum class ActionKind {
    CREATE_TASK,
    CREATE_REMINDER,
    SCHEDULE_EVENT,
    SHARE_TEXT,
    OPEN_FILE,
}

/** One concrete execution item derived from a workflow step or bundle. */
data class ActionDescriptor(
    val kind: ActionKind,
    val title: String,
    val detail: String,
    /** Exact time for time-bound actions (e.g. reminder epoch millis). */
    val atMillis: Long? = null,
)

/** Result of running one action; never faked. */
sealed interface ActionResult {
    data class Success(val message: String) : ActionResult
    data class Failed(val reason: String) : ActionResult
}

/**
 * Executes workflow steps with real Android mechanisms: alarm-scheduled
 * reminder notifications, calendar insert intents, the system share sheet and
 * file views. Local task persistence itself goes through the repository.
 * Anything requiring the user to finish in another app reports exactly that.
 */
class ActionEngine(
    private val context: Context,
    private val notifier: FlowOSNotifier,
) {

    /** Builds the execution checklist shown on the Execute screen. */
    fun buildActions(
        tasks: List<ExtractedTask>,
        includeCalendarEvent: Boolean = false,
    ): List<ActionDescriptor> {
        val actions = mutableListOf<ActionDescriptor>()
        tasks.forEach { task ->
            actions += ActionDescriptor(
                kind = ActionKind.CREATE_TASK,
                title = task.title,
                detail = "Saved to your local task list",
            )
            if (task.deadlineEpochMillis != null) {
                actions += ActionDescriptor(
                    kind = ActionKind.CREATE_REMINDER,
                    title = "Reminder: ${task.title}",
                    detail = "Notification scheduled before the deadline",
                )
            }
            if (task.requiresSharing || task.person != null) {
                actions += ActionDescriptor(
                    kind = ActionKind.SHARE_TEXT,
                    title = "Share: ${task.title}",
                    detail = task.person?.let { "Prepared for $it" } ?: "Opens the share sheet",
                )
            }
            if (task.requiresFile) {
                actions += ActionDescriptor(
                    kind = ActionKind.OPEN_FILE,
                    title = "File ready: ${task.title}",
                    detail = "Opens the linked file when executed",
                )
            }
        }
        if (includeCalendarEvent) {
            tasks.firstOrNull { it.deadlineEpochMillis != null }?.let { task ->
                actions += ActionDescriptor(
                    kind = ActionKind.SCHEDULE_EVENT,
                    title = "Event: ${task.title}",
                    detail = "Drafted in your calendar app",
                )
            }
        }
        return actions
    }

    /** Schedules a reminder notification via AlarmManager. */
    fun scheduleReminder(title: String, deadlineEpochMillis: Long): ActionResult =
        notifier.scheduleDeadlineReminder(title, deadlineEpochMillis)

    /**
     * Opens the calendar app pre-filled with the event; the user confirms
     * creation there (that is the confirmation step — we never write to the
     * calendar provider directly, so no WRITE_CALENDAR permission is needed).
     */
    fun scheduleCalendarEvent(title: String, deadlineEpochMillis: Long): ActionResult {
        val intent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, deadlineEpochMillis)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, deadlineEpochMillis + 60 * 60 * 1000L)
            .putExtra(CalendarContract.Events.TITLE, title)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if (intent.resolveActivity(context.packageManager) == null) {
            return ActionResult.Failed("No calendar app available — deadline saved on the task")
        }
        return try {
            context.startActivity(intent)
            ActionResult.Success("Event drafted in calendar — confirm it there")
        } catch (_: SecurityException) {
            ActionResult.Failed("Calendar app denied the request")
        } catch (_: ActivityNotFoundException) {
            ActionResult.Failed("No calendar app available")
        }
    }

    /** Opens the system share sheet; the user picks the target app. */
    fun shareText(text: String): ActionResult =
        try {
            val intent = Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, text)
            context.startActivity(
                Intent.createChooser(intent, "Share via")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
            ActionResult.Success("Share sheet opened")
        } catch (_: Exception) {
            ActionResult.Failed("No app available for sharing")
        }

    /** Opens a file with a viewer if one exists on the device. */
    fun openFile(path: String): ActionResult {
        val file = File(path)
        if (!file.exists()) return ActionResult.Failed("File not found: ${file.name}")
        return try {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)
            val mimeType = guessMimeType(file.name)
            val intent = Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, mimeType)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                ActionResult.Success("Opened ${file.name}")
            } else {
                ActionResult.Failed("No app can open ${file.name}")
            }
        } catch (e: Exception) {
            ActionResult.Failed("Couldn't open ${file.name}: ${e.message}")
        }
    }

    fun hasNotificationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    /** True when some app can handle calendar event insertion. */
    fun hasCalendarApp(): Boolean {
        val intent = Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)
        return intent.resolveActivity(context.packageManager) != null
    }

    private fun guessMimeType(name: String): String = when {
        name.endsWith(".pdf", true) -> "application/pdf"
        name.endsWith(".txt", true) -> "text/plain"
        name.endsWith(".png", true) -> "image/png"
        name.endsWith(".jpg", true) || name.endsWith(".jpeg", true) -> "image/jpeg"
        else -> "application/octet-stream"
    }
}
