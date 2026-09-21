package com.flowos.app.capture

import com.flowos.app.domain.model.AIAnalysisResult
import com.flowos.app.domain.model.CaptureDraft
import com.flowos.app.domain.model.IntentType
import com.flowos.app.domain.model.Priority
import com.flowos.app.util.TimeParser
import java.time.LocalDateTime

/**
 * Specialized understanding engine for screen captures (Flow Snap).
 * Focuses on high-density information extraction from varied screen layouts.
 */
object FlowSnapIntelligenceEngine {

    data class SnapInsight(
        val summary: String,
        val detectedDate: Long? = null,
        val location: String? = null,
        val category: String = "INFO",
        val suggestedActions: List<String> = emptyList()
    )

    fun analyze(text: String): SnapInsight {
        val lower = text.lowercase()
        val now = LocalDateTime.now()
        
        // 1. Extract Date/Deadline
        val deadline = TimeParser.parseDeadline(text, now)
        
        // 2. Extract Location (Naive heuristic)
        val location = extractLocation(text)
        
        // 3. Determine Category & Suggested Actions
        return when {
            lower.contains("exam") || lower.contains("test") -> SnapInsight(
                summary = "Exam scheduled: ${text.take(40)}...",
                detectedDate = deadline,
                location = location,
                category = "EXAM",
                suggestedActions = listOf("ADD TO CALENDAR", "CREATE STUDY PLAN")
            )
            lower.contains("deadline") || lower.contains("submit") -> SnapInsight(
                summary = "Submission deadline detected.",
                detectedDate = deadline,
                category = "DEADLINE",
                suggestedActions = listOf("SET REMINDER", "CREATE OUTCOME")
            )
            lower.contains("meeting") || lower.contains("call") -> SnapInsight(
                summary = "Meeting invitation.",
                detectedDate = deadline,
                location = location,
                category = "MEETING",
                suggestedActions = listOf("ADD TO CALENDAR", "PREPARE NOTES")
            )
            else -> SnapInsight(
                summary = "Information captured.",
                detectedDate = deadline,
                category = "INFO",
                suggestedActions = listOf("SAVE TO FLOW SPACE", "SET REMINDER")
            )
        }
    }

    private fun extractLocation(text: String): String? {
        val locationKeywords = listOf("Block", "Room", "Floor", "Hall", "Office", "Building")
        locationKeywords.forEach { keyword ->
            val regex = Regex("""$keyword\s+([A-Z0-9]+)""")
            val match = regex.find(text)
            if (match != null) return match.groupValues[0]
        }
        return null
    }
}
