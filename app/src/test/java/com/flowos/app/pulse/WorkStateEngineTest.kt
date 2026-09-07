package com.flowos.app.pulse

import com.flowos.app.data.local.ProjectEntity
import com.flowos.app.data.local.TaskEntity
import com.flowos.app.domain.model.Priority
import com.flowos.app.domain.model.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests for the deterministic FlowPulse engines. */
class WorkStateEngineTest {

    private val engine = WorkStateEngine()
    private val now = 1_700_000_000_000L

    private fun task(
        id: String,
        title: String = id,
        priority: Priority = Priority.MEDIUM,
        deadline: Long? = null,
        status: TaskStatus = TaskStatus.ACTIVE,
        orderIndex: Int = 0,
        projectId: String = "p1",
        person: String? = null,
    ): TaskEntity = TaskEntity(
        id = id,
        title = title,
        description = "",
        status = status.name,
        priority = priority.name,
        deadlineEpochMillis = deadline,
        deadlineLabel = deadline?.let { "label" },
        personName = person,
        projectId = projectId,
        orderIndex = orderIndex,
        createdAt = now,
    )

    private fun project(id: String = "p1", name: String = "Alpha") = ProjectEntity(
        id = id,
        name = name,
        colorHex = "#FFD400",
        progressPercent = 0,
        createdAt = now,
    )

    @Test
    fun `empty data yields empty work state`() {
        val state = engine.compute(
            projects = emptyList(),
            tasks = emptyList(),
            dependencies = emptyList(),
            calendarEvents = emptyList(),
            nowMillis = now,
        )
        assertNull(state.activeThread)
        assertNull(state.nextBestAction)
    }

    @Test
    fun `project with nearest deadline becomes active thread`() {
        val soon = now + 2 * HOUR
        val late = now + 5 * DAY
        val state = engine.compute(
            projects = listOf(project("p1", "Soon"), project("p2", "Late")),
            tasks = listOf(
                task("t1", projectId = "p1", deadline = soon),
                task("t2", projectId = "p2", deadline = late),
            ),
            dependencies = emptyList(),
            calendarEvents = emptyList(),
            nowMillis = now,
        )
        assertEquals("Soon", state.projectName)
        assertEquals("t1", state.nextBestAction?.taskId)
    }

    @Test
    fun `next best action is the blocker that unblocks downstream work`() {
        // a → b → c chain; all open. "a" unblocks two tasks, so it wins.
        val state = engine.compute(
            projects = listOf(project()),
            tasks = listOf(
                task("a", orderIndex = 0, deadline = now + DAY),
                task("b", orderIndex = 1, deadline = now + DAY),
                task("c", orderIndex = 2, deadline = now + DAY),
            ),
            dependencies = listOf(
                TaskDependencyEdge("a", "b"),
                TaskDependencyEdge("b", "c"),
            ),
            calendarEvents = emptyList(),
            nowMillis = now,
        )
        assertEquals("a", state.nextBestAction?.taskId)
        assertTrue(state.nextBestAction!!.unlocksTaskTitles.isNotEmpty())
        assertTrue(state.nextBestAction!!.reason.contains("unblocks"))
    }

    @Test
    fun `completion percentage reflects real task data`() {
        val state = engine.compute(
            projects = listOf(project()),
            tasks = listOf(
                task("done1", status = TaskStatus.DONE),
                task("done2", status = TaskStatus.DONE),
                task("open1"),
                task("open2"),
            ),
            dependencies = emptyList(),
            calendarEvents = emptyList(),
            nowMillis = now,
        )
        assertEquals(50, state.completionPercentage)
    }

    @Test
    fun `blocked tasks are derived from dependency edges`() {
        val state = engine.compute(
            projects = listOf(project()),
            tasks = listOf(
                task("blocker", orderIndex = 0),
                task("blocked", orderIndex = 1),
                task("free", orderIndex = 2),
            ),
            dependencies = listOf(TaskDependencyEdge("blocker", "blocked")),
            calendarEvents = emptyList(),
            nowMillis = now,
        )
        val blockedIds = state.blockedTasks.map { it.id }.toSet()
        assertEquals(setOf("blocked"), blockedIds)
    }

    @Test
    fun `same input produces same output deterministically`() {
        fun build(): WorkState = engine.compute(
            projects = listOf(project()),
            tasks = listOf(
                task("a", orderIndex = 1, deadline = now + DAY),
                task("b", orderIndex = 0, deadline = now + DAY),
                task("c", orderIndex = 2),
            ),
            dependencies = listOf(TaskDependencyEdge("a", "b")),
            calendarEvents = emptyList(),
            nowMillis = now,
        )
        val first = build()
        val second = build()
        assertEquals(first.nextBestAction?.taskId, second.nextBestAction?.taskId)
        assertEquals(first.projectName, second.projectName)
        assertEquals(first.completionPercentage, second.completionPercentage)
    }

    @Test
    fun `high priority task with imminent deadline wins over plain blocker`() {
        val urgent = now + HOUR
        val state = engine.compute(
            projects = listOf(project()),
            tasks = listOf(
                task("blocker", orderIndex = 0, deadline = now + 2 * DAY),
                task("urgent", orderIndex = 1, priority = Priority.HIGH, deadline = urgent),
            ),
            dependencies = listOf(TaskDependencyEdge("blocker", "urgent")),
            calendarEvents = emptyList(),
            nowMillis = now,
        )
        // urgent unblocks nothing but is due within 6h → its deadline score (40)
        // beats the blocker's unblock score (25).
        assertEquals("urgent", state.nextBestAction?.taskId)
        assertNotNull(state.nextBestAction)
    }

    private companion object {
        const val HOUR = 60L * 60 * 1000
        const val DAY = 24 * HOUR
    }
}
