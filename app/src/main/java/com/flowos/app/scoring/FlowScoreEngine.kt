package com.flowos.app.scoring

import com.flowos.app.data.local.FlowScoreEntity
import com.flowos.app.data.local.TaskEntity
import java.util.UUID

/**
 * Calculates the multi-dimensional FlowScore.
 * Focuses on effective progress and reliability, not just volume of tasks.
 */
object FlowScoreEngine {

    fun compute(
        tasks: List<TaskEntity>,
        completedToday: List<TaskEntity>,
        overranCount: Int,
        totalFocusMinutes: Int
    ): FlowScoreEntity {
        
        // 1. Outcome Progress (0-100)
        val progress = if (tasks.isEmpty()) 0 else (tasks.count { it.completedAt != null } * 100) / tasks.size
        
        // 2. Focus Efficiency (0-100)
        // High if actual duration <= estimated duration
        val efficientCount = completedToday.count { it.actualDurationMinutes <= it.estimatedDurationMinutes }
        val efficiency = if (completedToday.isEmpty()) 0 else (efficientCount * 100) / completedToday.size
        
        // 3. Plan Reliability (0-100)
        // Low if many overruns
        val reliability = (100 - (overranCount * 15)).coerceIn(0, 100)
        
        // 4. Time Utilization (0-100)
        val utilization = (totalFocusMinutes * 100 / 480).coerceIn(0, 100) // Assumes 8h day
        
        // 5. Recovery (Simple heuristic for MVP)
        val recovery = 70 

        val total = (progress * 0.3 + efficiency * 0.25 + reliability * 0.2 + utilization * 0.15 + recovery * 0.1).toInt()

        val insight = when {
            overranCount > 2 -> "Your task estimates were 20-30% shorter than actual execution today."
            efficiency > 80 -> "Excellent focus efficiency today. Your estimates are highly accurate."
            progress < 30 -> "Plan suggests focusing on high-impact outcomes to boost progress."
            else -> "Steady progress. Maintain focus on the critical path."
        }

        return FlowScoreEntity(
            id = "fs_${UUID.randomUUID()}",
            totalScore = total,
            outcomeProgress = progress,
            focusEfficiency = efficiency,
            planReliability = reliability,
            timeUtilization = utilization,
            frictionHandling = (100 - overranCount * 10).coerceIn(0, 100),
            recovery = recovery,
            insight = insight,
            timestamp = System.currentTimeMillis()
        )
    }
}
