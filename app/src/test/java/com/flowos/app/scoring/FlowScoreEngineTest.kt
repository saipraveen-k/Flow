package com.flowos.app.scoring

import com.flowos.app.context.FitnessMetrics
import com.flowos.app.context.HealthConnectStatus
import com.flowos.app.data.local.TaskEntity
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FlowScoreEngineTest {

    @Test
    fun `compute returns valid score entity with insight and realistic range`() {
        val tasks = listOf(
            TaskEntity(
                id = "t1",
                title = "Complete architecture draft",
                status = "COMPLETED",
                priority = "HIGH",
                createdAt = System.currentTimeMillis(),
                completedAt = System.currentTimeMillis()
            )
        )
        val fitnessMetrics = FitnessMetrics(
            steps = 8000,
            workoutDurationMinutes = 45,
            status = HealthConnectStatus.SUCCESS
        )

        val entity = FlowScoreEngine.compute(
            tasks = tasks,
            completedToday = tasks,
            overranCount = 0,
            totalFocusMinutes = 240,
            fitnessMetrics = fitnessMetrics
        )

        assertTrue(entity.totalScore in 0..100)
        assertTrue(entity.recovery >= 80)
        assertNotNull(entity.insight)
    }
}
