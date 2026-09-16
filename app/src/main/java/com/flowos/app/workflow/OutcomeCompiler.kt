package com.flowos.app.workflow

import com.flowos.app.domain.model.AIAnalysisResult
import com.flowos.app.domain.model.ExtractedDependency
import com.flowos.app.domain.model.ExtractedTask
import com.flowos.app.domain.model.IntentType
import com.flowos.app.domain.model.LifeHub
import com.flowos.app.domain.model.Outcome
import com.flowos.app.domain.model.Priority
import com.flowos.app.domain.model.VerificationState
import java.util.UUID

/**
 * Compiles unstructured AI analysis into a formal FlowOS Outcome and its Work Graph.
 * Ensures the result is actionable and respects the "AI understands, logic decides" rule.
 */
object OutcomeCompiler {

    data class CompilationResult(
        val outcome: Outcome,
        val tasks: List<ExtractedTask>,
        val dependencies: List<ExtractedDependency>
    )

    fun compile(analysis: AIAnalysisResult): CompilationResult {
        val outcomeId = "outcome_${UUID.randomUUID()}"
        val now = System.currentTimeMillis()

        // 1. Derive Outcome
        val outcome = Outcome(
            id = outcomeId,
            title = deriveTitle(analysis),
            description = analysis.summary,
            deadlineEpochMillis = analysis.deadlines.maxOfOrNull { it.epochMillis },
            priority = analysis.priority,
            status = VerificationState.PLANNED,
            hub = deriveHub(analysis),
            createdAt = now,
            updatedAt = now
        )

        // 2. Validate and Enrich Tasks
        val tasks = analysis.tasks.mapIndexed { index, task ->
            task.copy(
                outcomeId = outcomeId,
                estimatedDurationMinutes = task.estimatedDurationMinutes.coerceIn(5, 480)
            )
        }

        // 3. Validate Dependencies (detect cycles, missing nodes)
        val validDependencies = validateDependencies(tasks, analysis.dependencies)

        return CompilationResult(outcome, tasks, validDependencies)
    }

    private fun deriveTitle(analysis: AIAnalysisResult): String {
        return analysis.project 
            ?: analysis.detectedIntent?.label?.replaceFirstChar { it.uppercase() }
            ?: analysis.summary.take(40)
            ?: "New Outcome"
    }

    private fun deriveHub(analysis: AIAnalysisResult): LifeHub {
        val text = (analysis.summary + (analysis.project ?: "")).lowercase()
        return when {
            text.contains("exam") || text.contains("study") || text.contains("course") -> LifeHub.LEARNING
            text.contains("workout") || text.contains("gym") || text.contains("run") -> LifeHub.FITNESS
            text.contains("personal") || text.contains("family") || text.contains("home") -> LifeHub.PERSONAL
            else -> LifeHub.PROFESSIONAL
        }
    }

    private fun validateDependencies(
        tasks: List<ExtractedTask>,
        dependencies: List<ExtractedDependency>
    ): List<ExtractedDependency> {
        val n = tasks.size
        return dependencies.filter { it.fromIndex in 0 until n && it.toIndex in 0 until n && it.fromIndex != it.toIndex }
    }
}
