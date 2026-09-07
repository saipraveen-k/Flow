package com.flowos.app.workflow

import com.flowos.app.domain.model.*
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkflowPlannerTest {

    @Test
    fun testLinearDependency() {
        val tasks = listOf(
            ExtractedTask(title = "Task 1"),
            ExtractedTask(title = "Task 2")
        )
        val deps = listOf(ExtractedDependency(0, 1, "1 before 2"))
        val analysis = AIAnalysisResult(
            summary = "", intent = IntentType.UNKNOWN, priority = Priority.MEDIUM,
            people = emptyList(), deadlines = emptyList(), tasks = tasks,
            dependencies = deps, project = null, confidence = 0.0, sourceType = SourceType.TEXT
        )

        val plan = WorkflowPlanner.plan(analysis)
        assertEquals("Task 1", plan.steps[0].title)
        assertEquals("Task 2", plan.steps[1].title)
    }

    @Test
    fun testTieBreakDeadline() {
        val tasks = listOf(
            ExtractedTask(title = "Later Task", deadlineEpochMillis = 2000),
            ExtractedTask(title = "Earlier Task", deadlineEpochMillis = 1000)
        )
        val analysis = AIAnalysisResult(
            summary = "", intent = IntentType.UNKNOWN, priority = Priority.MEDIUM,
            people = emptyList(), deadlines = emptyList(), tasks = tasks,
            dependencies = emptyList(), project = null, confidence = 0.0, sourceType = SourceType.TEXT
        )

        val plan = WorkflowPlanner.plan(analysis)
        assertEquals("Earlier Task", plan.steps[0].title)
        assertEquals("Later Task", plan.steps[1].title)
    }

    @Test
    fun testCycleDetection() {
        val tasks = listOf(
            ExtractedTask(title = "A"),
            ExtractedTask(title = "B")
        )
        // Cycle: A -> B and B -> A
        val deps = listOf(
            ExtractedDependency(0, 1, ""),
            ExtractedDependency(1, 0, "")
        )
        val analysis = AIAnalysisResult(
            summary = "", intent = IntentType.UNKNOWN, priority = Priority.MEDIUM,
            people = emptyList(), deadlines = emptyList(), tasks = tasks,
            dependencies = deps, project = null, confidence = 0.0, sourceType = SourceType.TEXT
        )

        val plan = WorkflowPlanner.plan(analysis)
        // Should not crash and contain both tasks
        assertEquals(2, plan.steps.size)
    }
}
