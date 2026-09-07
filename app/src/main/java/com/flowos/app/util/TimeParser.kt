package com.flowos.app.util

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/**
 * Deterministic parser for the time expressions the demo and everyday captures use:
 * "tonight", "tomorrow at 10 am", "september 10 at 2 pm", "friday", "in 3 days", "next week".
 *
 * Kept separate from the AI engine on purpose: deadline grounding must stay rule-based,
 * cheap and offline.
 */
object TimeParser {

    private val timePattern = Regex("""(\d{1,2})(?::(\d{2}))?\s*(am|pm)?""", RegexOption.IGNORE_CASE)
    private val monthNames = mapOf(
        "january" to 1, "february" to 2, "march" to 3, "april" to 4,
        "may" to 5, "june" to 6, "july" to 7, "august" to 8,
        "september" to 9, "october" to 10, "november" to 11, "december" to 12,
    )
    private val dayNames = mapOf(
        "monday" to DayOfWeek.MONDAY,
        "tuesday" to DayOfWeek.TUESDAY,
        "wednesday" to DayOfWeek.WEDNESDAY,
        "thursday" to DayOfWeek.THURSDAY,
        "friday" to DayOfWeek.FRIDAY,
        "saturday" to DayOfWeek.SATURDAY,
        "sunday" to DayOfWeek.SUNDAY,
    )

    /**
     * Parse a natural-language deadline expression found in [text].
     * @return epoch millis for the best matching time, or null when nothing matches.
     */
    fun parseDeadline(text: String, now: LocalDateTime = LocalDateTime.now()): Long? {
        val lower = text.lowercase()
        val time = parseExplicitTime(lower)

        // Longest / most specific patterns first.
        monthNames.forEach { (name, month) ->
            val regex = Regex("""$name\s+(\d{1,2})(?:st|nd|rd|th)?""")
            regex.find(lower)?.let { match ->
                val day = match.groupValues[1].toInt()
                val date = safeDate(now.year, month, day) ?: return@let
                return at(date, time ?: defaultTimeFor(monthDayIsPast(date, now)), now)
            }
        }

        dayNames.forEach { (name, day) ->
            if (Regex("""\b$name\b""").containsMatchIn(lower)) {
                val date = now.toLocalDate().with(TemporalAdjusters.next(day))
                return at(date, time ?: LocalTime.of(9, 0), now)
            }
        }

        when {
            Regex("""\btonight\b""").containsMatchIn(lower) ->
                return at(now.toLocalDate(), time ?: LocalTime.of(20, 0), now)

            Regex("""\btomorrow\b""").containsMatchIn(lower) ->
                return at(now.toLocalDate().plusDays(1), time ?: LocalTime.of(9, 0), now)

            Regex("""\btoday\b|\bthis evening\b""").containsMatchIn(lower) ->
                return at(now.toLocalDate(), time ?: LocalTime.of(18, 0), now)

            Regex("""next\s+week\b""").containsMatchIn(lower) ->
                return at(now.toLocalDate().plusWeeks(1), time ?: LocalTime.of(9, 0), now)
        }

        Regex("""in\s+(\d{1,2})\s+days?""").find(lower)?.let { match ->
            val days = match.groupValues[1].toInt()
            return at(now.toLocalDate().plusDays(days.toLong()), time ?: LocalTime.of(9, 0), now)
        }

        return null
    }

    /** True when the text mentions any known time expression. */
    fun mentionsTime(text: String): Boolean {
        val lower = text.lowercase()
        val keyword = Regex("""\b(tonight|tomorrow|today|next week|next month|evening|morning)\b""")
        val monthDay = monthNames.keys.any { month -> Regex("""$month\s+\d{1,2}""").containsMatchIn(lower) }
        val dayName = dayNames.keys.any { day -> Regex("""\b$day\b""").containsMatchIn(lower) }
        val explicit = timePattern.containsMatchIn(lower)
        return keyword.containsMatchIn(lower) || monthDay || dayName || explicit
    }

    private fun parseExplicitTime(lower: String): LocalTime? {
        // 1. Try "at 10 am", "10:30 pm", etc.
        val complexMatch = Regex("""(?:at\s+)?(\d{1,2})(?::(\d{2}))?\s*(am|pm)""", RegexOption.IGNORE_CASE).find(lower)
        if (complexMatch != null) {
            val hour = complexMatch.groupValues[1].toIntOrNull() ?: return null
            val minute = complexMatch.groupValues[2].takeIf { it.isNotEmpty() }?.toIntOrNull() ?: 0
            val meridiem = complexMatch.groupValues[3].lowercase()
            val h24 = when {
                meridiem == "pm" && hour != 12 -> hour + 12
                meridiem == "am" && hour == 12 -> 0
                else -> hour
            }
            return if (h24 in 0..23 && minute in 0..59) LocalTime.of(h24, minute) else null
        }

        // 2. Try "10:30" (24h)
        val simpleMatch = Regex("""(\d{1,2}):(\d{2})""").find(lower)
        if (simpleMatch != null) {
            val h = simpleMatch.groupValues[1].toIntOrNull() ?: return null
            val m = simpleMatch.groupValues[2].toIntOrNull() ?: return null
            return if (h in 0..23 && m in 0..59) LocalTime.of(h, m) else null
        }

        return null
    }

    private fun monthDayIsPast(date: LocalDate, now: LocalDateTime): Boolean = date.isBefore(now.toLocalDate())

    private fun at(date: LocalDate, time: LocalTime, now: LocalDateTime): Long {
        var resolved = LocalDateTime.of(date, time)
        if (resolved.isBefore(now)) {
            // Deadline already passed today: roll to tomorrow so tasks stay actionable.
            resolved = resolved.plusDays(1)
        }
        return resolved.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun defaultTimeFor(isPast: Boolean): LocalTime =
        if (isPast) LocalTime.of(9, 0) else LocalTime.of(20, 0)

    private fun safeDate(year: Int, month: Int, day: Int): LocalDate? = try {
        LocalDate.of(year, month, day)
    } catch (_: Exception) {
        null
    }

    /** Compact human label like "Tonight · 8:00 PM" or "Sep 10 · 2:00 PM". */
    fun humanLabel(epochMillis: Long, now: LocalDate = LocalDate.now()): String {
        val dateTime = Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        val date = dateTime.toLocalDate()
        val time = dateTime.format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))
        return when (date) {
            now -> "Today · $time"
            now.plusDays(1) -> "Tomorrow · $time"
            else -> "${date.format(DateTimeFormatter.ofPattern("MMM d", Locale.US))} · $time"
        }
    }
}
