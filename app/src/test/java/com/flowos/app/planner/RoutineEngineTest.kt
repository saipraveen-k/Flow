package com.flowos.app.planner

import com.flowos.app.domain.model.CalendarEventModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RoutineEngineTest {

    private val routineEngine = RoutineEngine()

    @Test
    fun `default college day blocks contain expected routine entries`() {
        val blocks = routineEngine.getDefaultCollegeDayRoutineBlocks()
        assertEquals(7, blocks.size)
        assertTrue(blocks.any { it.blockType == RoutineBlockType.FITNESS })
        assertTrue(blocks.any { it.blockType == RoutineBlockType.COLLEGE_CLASSES })
    }

    @Test
    fun `calculateAvailableFocusMinutes deducts fixed blocks and events correctly`() {
        val blocks = routineEngine.getDefaultCollegeDayRoutineBlocks()
        val events = listOf(
            CalendarEventModel(
                eventId = 1L,
                title = "Team Sync",
                beginMillis = 1758427200000L, // 10:00 AM
                endMillis = 1758430800000L,   // 11:00 AM
                location = "Room 101",
                allDay = false
            )
        )
        val focusMinutes = routineEngine.calculateAvailableFocusMinutes(
            date = LocalDate.of(2026, 9, 21),
            routineBlocks = blocks,
            calendarEvents = events
        )
        assertTrue("Focus minutes should be positive and reduced by fixed blocks", focusMinutes > 0)
    }

    @Test
    fun `detectConflicts identifies overlapping calendar events and routine blocks`() {
        val blocks = listOf(
            RoutineBlockEntity("b1", "r1", "College Lecture", RoutineBlockType.COLLEGE_CLASSES, 9, 0, 11, 0, true, 1)
        )
        val zoneId = java.time.ZoneId.systemDefault()
        val date = LocalDate.of(2026, 9, 21)
        val startEpoch = java.time.LocalDateTime.of(2026, 9, 21, 10, 0).atZone(zoneId).toEpochSecond() * 1000
        val endEpoch = java.time.LocalDateTime.of(2026, 9, 21, 12, 0).atZone(zoneId).toEpochSecond() * 1000

        val events = listOf(
            CalendarEventModel(1L, "Exam Review", startEpoch, endEpoch, "Hall B", null, false)
        )

        val conflicts = routineEngine.detectConflicts(date, blocks, events)
        assertEquals(1, conflicts.size)
        assertEquals("College Lecture", conflicts[0].routineBlockTitle)
        assertEquals("Exam Review", conflicts[0].calendarEventTitle)
    }
}
