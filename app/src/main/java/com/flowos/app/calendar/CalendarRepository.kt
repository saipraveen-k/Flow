package com.flowos.app.calendar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.flowos.app.domain.model.CalendarEventModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads the device calendar for FlowPulse intelligence. Privacy rules:
 *  - READ_CALENDAR is never requested at startup; the Plan screen asks
 *    contextually when the user chooses to connect their calendar.
 *  - Without permission this repository returns an empty list — it never
 *    queries, never caches and never guesses.
 *  - Events are read on demand only (no polling), per the performance rules.
 */
class CalendarRepository(private val context: Context) {

    fun hasReadPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * Reads events starting within [horizonDays] from [nowMillis], ordered by
     * begin time. Empty result when permission is missing or the provider is
     * unavailable — callers must treat that as "no data", not as an error.
     */
    suspend fun readUpcomingEvents(nowMillis: Long, horizonDays: Int = 14): List<CalendarEventModel> {
        if (!hasReadPermission()) return emptyList()
        return withContext(Dispatchers.IO) { queryEvents(nowMillis, horizonDays) }
    }

    private fun queryEvents(nowMillis: Long, horizonDays: Int): List<CalendarEventModel> {
        val endMillis = nowMillis + horizonDays.toLong() * DAY_MILLIS
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
            .appendQueryParameter(CalendarContract.Instances.START_SEARCH_BEGIN, nowMillis.toString())
            .appendQueryParameter(CalendarContract.Instances.START_SEARCH_END, endMillis.toString())
            .build()
        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.DESCRIPTION,
        )

        return try {
            context.contentResolver.query(uri, projection, null, null, CalendarContract.Instances.BEGIN + " ASC")
                ?.use { cursor ->
                    val idIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
                    val titleIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
                    val beginIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
                    val endIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.END)
                    val allDayIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.ALL_DAY)
                    val locationIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_LOCATION)
                    val descIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.DESCRIPTION)

                    val events = mutableListOf<CalendarEventModel>()
                    while (cursor.moveToNext()) {
                        val begin = cursor.getLong(beginIdx)
                        val end = cursor.getLong(endIdx)
                        events += CalendarEventModel(
                            eventId = cursor.getLong(idIdx),
                            title = cursor.getString(titleIdx) ?: "Untitled event",
                            beginMillis = begin,
                            endMillis = end,
                            location = cursor.getString(locationIdx)?.takeIf { it.isNotBlank() },
                            description = cursor.getString(descIdx)?.takeIf { it.isNotBlank() },
                            allDay = cursor.getInt(allDayIdx) == 1,
                        )
                    }
                    events
                } ?: emptyList()
        } catch (_: SecurityException) {
            emptyList()
        } catch (_: IllegalArgumentException) {
            emptyList() // No calendar provider on this device.
        }
    }

    private companion object {
        const val DAY_MILLIS = 24 * 60 * 60 * 1000L
    }
}
