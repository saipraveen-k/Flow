package com.flowos.app.scoring

import com.flowos.app.context.FitnessMetrics
import com.flowos.app.data.local.FlowScoreEntity
import com.flowos.app.data.local.TaskEntity
import java.util.UUID

/**
 * Calculates the multi-dimensional FlowScore.
 * Combines: Outcome Progress, Focus Efficiency, Plan Reliability, Time Utilization,
 * Friction Handling, and Recovery/Sustainability.
 *
 * Fitness and routines provide recovery context, but raw step/calorie activity is not
 * blindly rewarded. Every score includes human-understandable reasoning.
 */
object FlowScoreEngine {

    fun compute(
        tasks: List<TaskEntity>,
        completedToday: List<TaskEntity>,
        overranCount: Int,
        totalFocusMinutes: Int,
        fitnessMetrics: FitnessMetrics? = null
    ): FlowScoreEntity {

        // 1. Outcome Progress (0-100)
        val progress = if (tasks.isEmpty()) 0 else (tasks.count { it.completedAt != null } * 100) / tasks.size

        // 2. Focus Efficiency (0-100)
        val efficientCount = completedToday.count { it.actualDurationMinutes <= it.estimatedDurationMinutes }
        val efficiency = if (completedToday.isEmpty()) 80 else (efficientCount * 100) / completedToday.size

        // 3. Plan Reliability (0-100)
        val reliability = (100 - (overranCount * 15)).coerceIn(0, 100)

        // 4. Time Utilization (0-100)
        val utilization = (totalFocusMinutes * 100 / 480).coerceIn(0, 100) // Assumes 8h focus day

        // 5. Friction Handling (0-100)
        val frictionHandling = (100 - overranCount * 10).coerceIn(0, 100)

        // 6. Recovery / Sustainability (0-100)
        val workoutMinutes = fitnessMetrics?.workoutDurationMinutes ?: 0L
        val recovery = when {
            workoutMinutes in 30..90 -> 90 // Healthy balanced exercise
            workoutMinutes > 120 -> 75 // Heavy exertion, watch recovery
            fitnessMetrics?.steps != null && fitnessMetrics.steps > 5000 -> 85
            else -> 70 // Moderate baseline
        }

        val total = (
                progress * 0.25 +
                efficiency * 0.20 +
                reliability * 0.20 +
                utilization * 0.15 +
                frictionHandling * 0.10 +
                recovery * 0.10
        ).toInt().coerceIn(0, 100)

        val insight = when {
            overranCount > 2 -> "Your task estimates were shorter than actual execution today. Consider adding buffer."
            efficiency > 80 && progress > 50 -> "Outstanding execution and focus efficiency today. Outcome progress is on track."
            workoutMinutes in 30..90 -> "Protected workout time completed. High recovery and sustainable focus score."
            progress < 30 -> "Focus on high-impact outcomes on the critical path to boost progress."
            else -> "Balanced workflow. Maintain steady progress on open outcomes."
        }

        return FlowScoreEntity(
            id = "fs_${UUID.randomUUID()}",
            totalScore = total,
            outcomeProgress = progress,
            focusEfficiency = efficiency,
            planReliability = reliability,
            timeUtilization = utilization,
            frictionHandling = frictionHandling,
            recovery = recovery,
            insight = insight,
            timestamp = System.currentTimeMillis()
        )
    }
}
