package com.flowos.app.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.flowos.app.action.ActionResult

/**
 * Owns the reminder notification channel and AlarmManager scheduling.
 * If notifications are not permitted, scheduling reports a failure honestly
 * instead of silently pretending a reminder exists.
 */
class FlowOSNotifier(private val context: Context) {

    companion object {
        const val CHANNEL_REMINDERS = "flowos_reminders"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        private const val REQUEST_CODE_OFFSET = 4200
    }

    fun ensureChannel() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_REMINDERS,
            "Task reminders",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Deadline reminders for FlowOS tasks"
        }
        manager.createNotificationChannel(channel)
    }

    /** Returns success only when the alarm was actually registered. */
    fun scheduleDeadlineReminder(title: String, deadlineEpochMillis: Long): ActionResult {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            ?: return ActionResult.Failed("Alarms unavailable on this device")

        val intent = Intent(context, ReminderReceiver::class.java)
            .putExtra(EXTRA_TITLE, title)
            .putExtra(EXTRA_NOTIFICATION_ID, title.hashCode())
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (REQUEST_CODE_OFFSET + title.hashCode()).let { if (it < 0) -it else it },
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return try {
            // setExactAndAllowWhileIdle keeps demo reminders reliable; on API 31+
            // the system may downgrade without the exact-alarm permission, which
            // still delivers the reminder (slightly less precisely).
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, deadlineEpochMillis, pendingIntent)
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, deadlineEpochMillis, pendingIntent)
            }
            ActionResult.Success("Reminder scheduled")
        } catch (_: SecurityException) {
            ActionResult.Failed("Couldn't schedule the reminder")
        }
    }

    fun cancelReminder(title: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (REQUEST_CODE_OFFSET + title.hashCode()).let { if (it < 0) -it else it },
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        ) ?: return
        alarmManager.cancel(pendingIntent)
    }
}

/** Fires the deadline notification. Skips silently when permission is missing. */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(FlowOSNotifier.EXTRA_TITLE) ?: return
        val id = intent.getIntExtra(FlowOSNotifier.EXTRA_NOTIFICATION_ID, title.hashCode())
        ReminderNotificationHelper.show(context, id, title)
    }
}
