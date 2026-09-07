package com.flowos.app.ai

import com.flowos.app.domain.model.AIAnalysisResult
import com.flowos.app.domain.model.CaptureDraft
import com.flowos.app.domain.model.SourceType

/**
 * Contract for turning a raw capture into a structured [AIAnalysisResult].
 * Application logic never reads free-form text output from an engine.
 */
interface AIEngine {

    /** Machine-readable id, e.g. "mock" or "local-heuristic". */
    val id: String

    /** User-facing processing label; must never overstate on-device AI. */
    val processingLabel: String

    suspend fun analyze(draft: CaptureDraft): AIAnalysisResult
}
