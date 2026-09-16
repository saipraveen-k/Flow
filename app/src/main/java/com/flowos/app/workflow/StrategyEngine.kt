package com.flowos.app.workflow

import com.flowos.app.domain.model.Strategy
import com.flowos.app.domain.model.StrategyStep

/**
 * Predefined productivity patterns that produce structured work graphs.
 */
object StrategyEngine {

    val PREDEFINED_STRATEGIES = listOf(
        Strategy(
            id = "strat_deep_work",
            title = "Deep Work",
            description = "High-concentration focus blocks with structured checkpoints.",
            steps = listOf(
                StrategyStep("Define Objective", 1, 10, false),
                StrategyStep("Deep Focus Block", 2, 90, true),
                StrategyStep("Review & Iterate", 3, 15, false)
            )
        ),
        Strategy(
            id = "strat_exam_sprint",
            title = "Exam Sprint",
            description = "Study-break-recall-revision loop for high retention.",
            steps = listOf(
                StrategyStep("Active Recall Session", 1, 45, true),
                StrategyStep("Short Break", 2, 10, false),
                StrategyStep("Targeted Revision", 3, 30, true)
            )
        ),
        Strategy(
            id = "strat_project_launch",
            title = "Project Launch",
            description = "Fast-track from definition to launch.",
            steps = listOf(
                StrategyStep("Define Core Flow", 1, 30, true),
                StrategyStep("Build MVP", 2, 120, true),
                StrategyStep("Smoke Test", 3, 20, true),
                StrategyStep("Launch / Submit", 4, 10, false)
            )
        )
    )

    fun getStrategyById(id: String): Strategy? = PREDEFINED_STRATEGIES.find { it.id == id }
}
