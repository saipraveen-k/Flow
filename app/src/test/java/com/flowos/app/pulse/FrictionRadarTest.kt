package com.flowos.app.pulse

import com.flowos.app.data.local.TaskEntity
import com.flowos.app.planner.RoutineConflict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FrictionRadarTest {

    private val frictionRadar = FrictionRadar()

    @Test
    fun `detects PC unavailable friction when task requires desktop and PC is disconnected`() {
        val tasks = listOf(
            TaskEntity(
                id = "t1",
                title = "Edit PPT presentation on PC",
                status = "ACTIVE",
                priority = "HIGH",
                createdAt = System.currentTimeMillis()
            )
        )
        val alerts = frictionRadar.analyzeFriction(
            openTasks = tasks,
            routineConflicts = emptyList(),
            isPcConnected = false,
            availableCapacityMinutes = 300
        )

        assertEquals(1, alerts.size)
        assertEquals(FrictionType.PC_UNAVAILABLE, alerts[0].type)
        assertEquals(3, alerts[0].actions.size)
    }

    @Test
    fun `detects insufficient capacity friction when task durations exceed focus time`() {
        val tasks = listOf(
            TaskEntity(
                id = "t1",
                title = "Study for Math Exam",
                status = "ACTIVE",
                priority = "HIGH",
                estimatedDurationMinutes = 240,
                createdAt = System.currentTimeMillis()
            )
        )
        val alerts = frictionRadar.analyzeFriction(
            openTasks = tasks,
            routineConflicts = emptyList(),
            isPcConnected = true,
            availableCapacityMinutes = 120
        )

        assertTrue(alerts.any { it.type == FrictionType.INSUFFICIENT_CAPACITY })
    }

    @Test
    fun `detects routine conflict friction`() {
        val conflicts = listOf(
            RoutineConflict(
                routineBlockTitle = "Fitness Workout",
                calendarEventTitle = "Doctor Appointment",
                startTimeEpochMillis = 1000L,
                endTimeEpochMillis = 2000L,
                resolutionSuggestion = "Shift workout"
            )
        )
        val alerts = frictionRadar.analyzeFriction(
            openTasks = emptyList(),
            routineConflicts = conflicts,
            isPcConnected = true,
            availableCapacityMinutes = 300
        )

        assertTrue(alerts.any { it.type == FrictionType.ROUTINE_CONFLICT })
    }
}
