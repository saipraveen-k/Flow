package com.flowos.app.pulse

import com.flowos.app.data.local.TaskEntity
import com.flowos.app.domain.model.Priority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NextBestActionEngineTest {

    private val engine = NextBestActionEngine()
    private val now = 1_700_000_000_000L

    private fun task(
        id: String,
        orderIndex: Int = 0,
        deadline: Long? = null,
        priority: Priority = Priority.MEDIUM,
    ): TaskEntity = TaskEntity(
        id = id,
        title = "Task $id",
        description = "",
        status = "ACTIVE",
        priority = priority.name,
        deadlineEpochMillis = deadline,
        deadlineLabel = deadline?.let { "label" },
        projectId = "p1",
        orderIndex = orderIndex,
        createdAt = now,
    )

    @Test
    fun `no tasks yields no action`() {
        assertNull(engine.forTasks(emptyList(), emptyList(), "P", now))
    }

    @Test
    fun `blocker with downstream dependents is preferred`() {
        val action = engine.forTasks(
            openTasks = listOf(
                task("a", orderIndex = 1, deadline = now + DAY),
                task("b", orderIndex = 0, deadline = now + DAY),
            ),
            dependencies = listOf(TaskDependencyEdge("b", "a")),
            projectName = "P",
            nowMillis = now,
        )
        assertEquals("b", action?.taskId)
        assertEquals(listOf("Task a"), action?.unlocksTaskTitles)
        assertEquals("It unblocks \"Task a\".", action?.reason)
    }

    @Test
    fun `urgent deadline beats low-priority blocker when nothing is unblocked`() {
        val action = engine.forTasks(
            openTasks = listOf(
                task("chill", orderIndex = 0, deadline = now + 3 * DAY),
                task("urgent", orderIndex = 1, deadline = now + HOUR, priority = Priority.HIGH),
            ),
            dependencies = emptyList(),
            projectName = "P",
            nowMillis = now,
        )
        assertEquals("urgent", action?.taskId)
    }

    @Test
    fun `task without deadline still gets a project-based reason`() {
        val action = engine.forTasks(
            openTasks = listOf(task("solo", orderIndex = 0)),
            dependencies = emptyList(),
            projectName = "Atlas",
            nowMillis = now,
        )
        assertEquals("It's the next open step in Atlas.", action?.reason)
    }

    @Test
    fun `thread ordering places dependencies before dependents`() {
        val a = task("a", orderIndex = 2)
        val b = task("b", orderIndex = 1)
        val c = task("c", orderIndex = 0)
        val ordered = ThreadOrdering.order(
            tasks = listOf(c, b, a),
            edges = listOf(TaskDependencyEdge("a", "b"), TaskDependencyEdge("b", "c")),
        )
        assertEquals(listOf("a", "b", "c"), ordered.map { it.id })
    }

    private companion object {
        const val HOUR = 60L * 60 * 1000
        const val DAY = 24 * HOUR
    }
}
