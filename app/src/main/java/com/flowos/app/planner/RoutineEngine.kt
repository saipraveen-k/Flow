package com.flowos.app.planner

import com.flowos.app.domain.model.CalendarEventModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class RoutineEngine(private val routineDao: RoutineDao? = null) {

    suspend fun getDefaultRoutine(): RoutineEntity? = routineDao?.getDefaultRoutine()

    suspend fun getBlocksForRoutine(routineId: String): List<RoutineBlockEntity> =
        routineDao?.getBlocksForRoutine(routineId) ?: emptyList()

    fun getDefaultCollegeDayRoutineBlocks(): List<RoutineBlockEntity> {
        val routineId = "college_day_default"
        return listOf(
            RoutineBlockEntity("block_1", routineId, "Wake & Morning Prep", RoutineBlockType.WAKE, 5, 0, 6, 40, true, 1),
            RoutineBlockEntity("block_2", routineId, "Commute to College", RoutineBlockType.TRAVEL, 6, 40, 9, 0, true, 2),
            RoutineBlockEntity("block_3", routineId, "College Lectures & Labs", RoutineBlockType.COLLEGE_CLASSES, 9, 0, 17, 0, true, 3),
            RoutineBlockEntity("block_4", routineId, "Return Commute", RoutineBlockType.TRAVEL, 17, 0, 19, 0, true, 4),
            RoutineBlockEntity("block_5", routineId, "Fitness & Workout", RoutineBlockType.FITNESS, 19, 0, 20, 0, true, 5),
            RoutineBlockEntity("block_6", routineId, "Deep Study Session", RoutineBlockType.STUDY, 20, 0, 23, 0, false, 6),
            RoutineBlockEntity("block_7", routineId, "Sleep & Rest", RoutineBlockType.SLEEP, 23, 0, 5, 0, true, 7)
        )
    }

    fun getBuiltInRoutineBlocks(type: RoutineType): List<RoutineBlockEntity> {
        val rId = type.name.lowercase()
        return when (type) {
            RoutineType.COLLEGE_DAY -> getDefaultCollegeDayRoutineBlocks()
            RoutineType.EXAM_DAY -> listOf(
                RoutineBlockEntity("${rId}_1", rId, "Early Revision", RoutineBlockType.STUDY, 6, 0, 8, 30, true, 1),
                RoutineBlockEntity("${rId}_2", rId, "Exam Session", RoutineBlockType.COLLEGE_CLASSES, 9, 0, 13, 0, true, 2),
                RoutineBlockEntity("${rId}_3", rId, "Rest & Light Meal", RoutineBlockType.MEALS_REST, 13, 0, 15, 0, true, 3),
                RoutineBlockEntity("${rId}_4", rId, "Next Exam Prep", RoutineBlockType.STUDY, 15, 0, 21, 0, false, 4),
                RoutineBlockEntity("${rId}_5", rId, "Sleep", RoutineBlockType.SLEEP, 22, 0, 6, 0, true, 5)
            )
            RoutineType.PROJECT_DAY -> listOf(
                RoutineBlockEntity("${rId}_1", rId, "Wake", RoutineBlockType.WAKE, 7, 0, 8, 0, true, 1),
                RoutineBlockEntity("${rId}_2", rId, "Morning Sprint", RoutineBlockType.DEEP_WORK, 8, 30, 12, 30, false, 2),
                RoutineBlockEntity("${rId}_3", rId, "Lunch & Break", RoutineBlockType.MEALS_REST, 12, 30, 14, 0, true, 3),
                RoutineBlockEntity("${rId}_4", rId, "Afternoon Architecture", RoutineBlockType.DEEP_WORK, 14, 0, 18, 0, false, 4),
                RoutineBlockEntity("${rId}_5", rId, "Evening Gym", RoutineBlockType.FITNESS, 18, 30, 19, 30, true, 5),
                RoutineBlockEntity("${rId}_6", rId, "Sleep", RoutineBlockType.SLEEP, 23, 0, 7, 0, true, 6)
            )
            RoutineType.DEEP_WORK_DAY -> listOf(
                RoutineBlockEntity("${rId}_1", rId, "Deep Work Block 1", RoutineBlockType.DEEP_WORK, 8, 0, 12, 0, false, 1),
                RoutineBlockEntity("${rId}_2", rId, "Lunch", RoutineBlockType.MEALS_REST, 12, 0, 13, 0, true, 2),
                RoutineBlockEntity("${rId}_3", rId, "Deep Work Block 2", RoutineBlockType.DEEP_WORK, 13, 30, 17, 30, false, 3)
            )
            RoutineType.FITNESS_DAY -> listOf(
                RoutineBlockEntity("${rId}_1", rId, "Morning Cardio", RoutineBlockType.FITNESS, 6, 30, 8, 0, true, 1),
                RoutineBlockEntity("${rId}_2", rId, "Work", RoutineBlockType.STUDY, 9, 0, 17, 0, false, 2),
                RoutineBlockEntity("${rId}_3", rId, "Evening Strength", RoutineBlockType.FITNESS, 18, 0, 19, 30, true, 3)
            )
            RoutineType.CUSTOM_DAY -> listOf(
                RoutineBlockEntity("${rId}_1", rId, "Custom Focus", RoutineBlockType.CUSTOM, 9, 0, 17, 0, false, 1)
            )
        }
    }

    /**
     * Calculates available uncommitted focus minutes for a given day by deducting fixed routine blocks
     * and external calendar events without silently overwriting either source.
     */
    fun calculateAvailableFocusMinutes(
        date: LocalDate,
        routineBlocks: List<RoutineBlockEntity>,
        calendarEvents: List<CalendarEventModel>
    ): Int {
        val totalMinutesInDay = 24 * 60
        var committedMinutes = 0

        val zoneId = ZoneId.systemDefault()
        val dayStartEpoch = date.atStartOfDay(zoneId).toEpochSecond() * 1000
        val dayEndEpoch = date.plusDays(1).atStartOfDay(zoneId).toEpochSecond() * 1000

        for (block in routineBlocks) {
            val startMin = block.startHour * 60 + block.startMinute
            var endMin = block.endHour * 60 + block.endMinute
            if (endMin <= startMin) endMin += 24 * 60 // spans overnight
            val duration = (endMin - startMin).coerceAtLeast(0)
            if (block.isFixed || block.blockType == RoutineBlockType.SLEEP || block.blockType == RoutineBlockType.TRAVEL) {
                committedMinutes += duration
            }
        }

        for (event in calendarEvents) {
            val clampedStart = event.beginMillis.coerceAtLeast(dayStartEpoch)
            val clampedEnd = event.endMillis.coerceAtMost(dayEndEpoch)
            if (clampedEnd > clampedStart) {
                val durationMin = ((clampedEnd - clampedStart) / 60_000).toInt()
                committedMinutes += durationMin
            }
        }

        return (totalMinutesInDay - committedMinutes).coerceAtLeast(0)
    }

    /**
     * Detects collisions between routine blocks and actual calendar events.
     */
    fun detectConflicts(
        date: LocalDate,
        routineBlocks: List<RoutineBlockEntity>,
        calendarEvents: List<CalendarEventModel>
    ): List<RoutineConflict> {
        val conflicts = mutableListOf<RoutineConflict>()
        val zoneId = ZoneId.systemDefault()

        for (block in routineBlocks) {
            val blockStart = LocalDateTime.of(date.year, date.monthValue, date.dayOfMonth, block.startHour, block.startMinute)
                .atZone(zoneId).toEpochSecond() * 1000
            val blockEnd = LocalDateTime.of(date.year, date.monthValue, date.dayOfMonth, block.endHour, block.endMinute)
                .atZone(zoneId).toEpochSecond() * 1000

            for (event in calendarEvents) {
                val hasOverlap = (event.beginMillis < blockEnd) && (event.endMillis > blockStart)
                if (hasOverlap) {
                    conflicts.add(
                        RoutineConflict(
                            routineBlockTitle = block.title,
                            calendarEventTitle = event.title,
                            startTimeEpochMillis = maxOf(blockStart, event.beginMillis),
                            endTimeEpochMillis = minOf(blockEnd, event.endMillis),
                            resolutionSuggestion = "Propose moving flexible task block or adjusting routine duration."
                        )
                    )
                }
            }
        }
        return conflicts
    }
}
