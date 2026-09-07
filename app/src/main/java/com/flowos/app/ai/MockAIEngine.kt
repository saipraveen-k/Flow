package com.flowos.app.ai

import com.flowos.app.domain.model.*
import com.flowos.app.util.TimeParser

/**
 * Deterministic demo engine optimized for the hackathon showcase.
 * It implements basic keyword-based extraction but handles the primary 
 * demo scenario with 100% reliability.
 */
class MockAIEngine : AIEngine {

    override val id: String = "mock-flagship"

    override val processingLabel: String = "Understanding your input..."

    override suspend fun analyze(draft: CaptureDraft): AIAnalysisResult {
        val text = draft.text.lowercase()
        
        // Primary Demo Scenario
        if (text.contains("architecture diagram") && text.contains("prakash")) {
            return buildDemoScenario(draft)
        }

        return fallbackAnalysis(draft)
    }

    private fun buildDemoScenario(draft: CaptureDraft): AIAnalysisResult {
        val tonight = TimeParser.parseDeadline("tonight") ?: (System.currentTimeMillis() + 4 * 3600000L)
        val tomorrow = TimeParser.parseDeadline("tomorrow at 10 am") ?: (System.currentTimeMillis() + 24 * 3600000L)

        val tasks = listOf(
            ExtractedTask(
                title = "Finish architecture diagram",
                description = "Complete the visual layout for the new system architecture.",
                priority = Priority.HIGH,
                deadlineEpochMillis = tonight,
                deadlineLabel = "Tonight",
                person = "You",
                project = "Architecture Review"
            ),
            ExtractedTask(
                title = "Send updated document to Prakash",
                description = "Export and share the architecture PDF with Prakash via email.",
                priority = Priority.HIGH,
                deadlineEpochMillis = tonight,
                deadlineLabel = "Tonight",
                person = "Prakash",
                project = "Architecture Review",
                requiresSharing = true
            ),
            ExtractedTask(
                title = "Prepare review notes",
                description = "Draft the key talking points for the upcoming project review meeting.",
                priority = Priority.MEDIUM,
                deadlineEpochMillis = tomorrow - 3600000L,
                deadlineLabel = "Before meeting",
                person = "You",
                project = "Architecture Review"
            ),
            ExtractedTask(
                title = "Project review",
                description = "Formal review session for the architecture diagram.",
                priority = Priority.MEDIUM,
                deadlineEpochMillis = tomorrow,
                deadlineLabel = "Tomorrow 10 AM",
                person = "You",
                project = "Architecture Review"
            )
        )

        return AIAnalysisResult(
            summary = "Architecture Review Workflow",
            intent = IntentType.TASK_ASSIGNMENT,
            priority = Priority.HIGH,
            people = listOf("You", "Prakash"),
            deadlines = listOf(
                ExtractedDeadline("Tonight", tonight, false),
                ExtractedDeadline("Tomorrow 10 AM", tomorrow, true)
            ),
            tasks = tasks,
            dependencies = listOf(
                ExtractedDependency(0, 1, "Finish the diagram first"),
                ExtractedDependency(1, 2, "Send it for review"),
                ExtractedDependency(2, 3, "Notes ready for the meeting")
            ),
            project = "Architecture Review",
            confidence = 0.94,
            sourceType = draft.sourceType,
            detectedIntent = IntentPredictor.predict(
                text = draft.text,
                projectName = "Architecture Review",
                deadlineEpochMillis = tomorrow,
            ),
        )
    }

    private fun fallbackAnalysis(draft: CaptureDraft): AIAnalysisResult {
        val sentences = draft.text.split(Regex("[.!?\n]+")).filter { it.isNotBlank() }
        val tasks = sentences.take(3).map { s ->
            val deadline = TimeParser.parseDeadline(s)
            ExtractedTask(
                title = s.trim().take(60),
                priority = if (s.contains("urgent", true)) Priority.HIGH else Priority.MEDIUM,
                deadlineEpochMillis = deadline,
                deadlineLabel = deadline?.let { TimeParser.humanLabel(it) }
            )
        }

        return AIAnalysisResult(
            summary = sentences.firstOrNull()?.take(50) ?: "Capture analysis",
            intent = IntentType.UNKNOWN,
            priority = Priority.MEDIUM,
            people = emptyList(),
            deadlines = tasks.mapNotNull { t -> 
                t.deadlineEpochMillis?.let { ExtractedDeadline(t.deadlineLabel ?: "", it, false) } 
            },
            tasks = tasks,
            dependencies = emptyList(),
            project = null,
            confidence = 0.45,
            sourceType = draft.sourceType,
            detectedIntent = IntentPredictor.predict(draft.text),
        )
    }
}
