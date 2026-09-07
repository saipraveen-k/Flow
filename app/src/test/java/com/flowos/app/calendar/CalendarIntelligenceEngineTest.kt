package com.flowos.app.calendar

import com.flowos.app.action.ActionBundleBuilder
import com.flowos.app.action.ActionKind
import com.flowos.app.data.local.ProjectEntity
import com.flowos.app.data.local.TaskEntity
import com.flowos.app.domain.model.CalendarEventModel
import com.flowos.app.domain.model.Priority
import com.flowos.app.domain.model.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests for deterministic calendar intelligence and bundle building. */
class CalendarIntelligenceEngineTest {

    private val engine = CalendarIntelligenceEngine()
    private val now = 1_700_000_000_000L

    private fun task(
        id: String,
        projectId: String?,
        deadline: Long?,
        status: TaskStatus = TaskStatus.ACTIVE,
        title: String = id,
    ): TaskEntity = TaskEntity(
        id = id,
        title = title,
        description = "",
        status = status.name,
        priority = Priority.MEDIUM.name,
        deadlineEpochMillis = deadline,
        deadlineLabel = deadline?.let { "label" },
        projectId = projectId,
        orderIndex = 0,
        createdAt = now,
    )

    private fun project(id: String, name: String) = ProjectEntity(
        id = id,
        name = name,
        colorHex = "#FFD400",
        progressPercent = 0,
        createdAt = now,
    )

    @Test
    fun `event title matching project name links the project`() {
        val event = CalendarEventModel(
            eventId = 1,
            title = "Architecture Review",
            beginMillis = now + DAY,
            endMillis = now + DAY + HOUR,
        )
        val matched = engine.matchProject(
            event = event,
            projects = listOf(project("p1", "Architecture Review")),
            tasks = emptyList(),
        )
        assertEquals("p1", matched?.id)
    }

    @Test
    fun `event near a project deadline links that project`() {
        val deadline = now + DAY
        val event = CalendarEventModel(
            eventId = 2,
            title = "Completely unrelated meeting title",
            beginMillis = deadline + HOUR, // within 12h window
            endMillis = deadline + 2 * HOUR,
        )
        val matched = engine.matchProject(
            event = event,
            projects = listOf(project("p1", "Alpha")),
            tasks = listOf(task("t1", "p1", deadline)),
        )
        assertEquals("p1", matched?.id)
    }

    @Test
    fun `preparation readiness derives from real task completion`() {
        val eventStart = now + 2 * DAY
        val event = CalendarEventModel(3, "Architecture Review", eventStart, eventStart + HOUR)

        val prep = engine.preparationsFor(
            events = listOf(event),
            projects = listOf(project("p1", "Architecture Review")),
            tasks = listOf(
                task("prep1", "p1", eventStart - HOUR, TaskStatus.DONE, title = "Prepare notes"),
                task("prep2", "p1", eventStart - HOUR, TaskStatus.ACTIVE, title = "Finish slides"),
                task("unrelated", "other", eventStart - HOUR, TaskStatus.ACTIVE),
            ),
            nowMillis = now,
        )

        assertEquals(1, prep.size)
        val preparation = prep.first()
        assertEquals(50, preparation.readinessPercent)
        assertEquals(1, preparation.prepTasks.size)
        assertEquals("Finish slides", preparation.nextPrepTask?.title)
    }

    @Test
    fun `event itself is excluded from its own preparation tasks`() {
        val eventStart = now + 2 * DAY
        val event = CalendarEventModel(4, "Architecture Review", eventStart, eventStart + HOUR)

        val prep = engine.preparationsFor(
            events = listOf(event),
            projects = listOf(project("p1", "Architecture Review")),
            tasks = listOf(
                task("the-event", "p1", eventStart, TaskStatus.ACTIVE, title = "Architecture Review"),
                task("real-prep", "p1", eventStart - HOUR, TaskStatus.ACTIVE, title = "Prepare notes"),
            ),
            nowMillis = now,
        )

        assertEquals(1, prep.size)
        assertEquals(listOf("real-prep"), prep.first().prepTasks.map { it.id })
    }

    @Test
    fun `events without a matching project produce no fake preparation`() {
        val event = CalendarEventModel(5, "Random lunch", now + DAY, now + DAY + HOUR)
        val prep = engine.preparationsFor(
            events = listOf(event),
            projects = listOf(project("p1", "Architecture Review")),
            tasks = emptyList(),
            nowMillis = now,
        )
        assertTrue(prep.isEmpty())
    }

    @Test
    fun `bundle for event without prep proposes deterministic starter checklist`() {
        val eventStart = now + DAY
        val event = CalendarEventModel(6, "Architecture Review", eventStart, eventStart + HOUR)
        val prep = engine.preparationsFor(
            events = listOf(event),
            projects = listOf(project("p1", "Architecture Review")),
            tasks = emptyList(),
            nowMillis = now,
        ).first()

        val bundle = ActionBundleBuilder.buildPreparationBundle(prep)
        val expected = ActionBundleBuilder.defaultChecklist("Architecture Review")
        assertEquals(expected, bundle.items.filter { it.kind == ActionKind.CREATE_TASK }.map { it.title })
        // Reminder + share are always appended.
        assertTrue(bundle.items.any { it.kind == ActionKind.CREATE_REMINDER })
        assertTrue(bundle.items.any { it.kind == ActionKind.SHARE_TEXT })
    }

    private companion object {
        const val HOUR = 60L * 60 * 1000
        const val DAY = 24 * HOUR
    }
}
