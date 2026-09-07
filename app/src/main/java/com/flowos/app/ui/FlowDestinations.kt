package com.flowos.app.ui

/**
 * All navigation routes in one place — no string scattering across screens.
 *
 * Top-level tabs: HOME, PLAN, FLOW, CONTEXT, ACTIVITY.
 * Capture loop: CAPTURE → PROCESSING → UNDERSTANDING → WORKFLOW → EXECUTE.
 * FOCUS is pushed from Home / Flow. SETTINGS from Home.
 */
object FlowDestinations {
    const val HOME = "home"
    const val PLAN = "plan"
    const val FLOW = "flow"
    const val CONTEXT = "context"
    const val ACTIVITY = "activity"

    const val CAPTURE = "capture"
    const val PROCESSING = "processing"
    const val UNDERSTANDING = "understanding"
    const val WORKFLOW = "workflow"
    const val EXECUTE = "execute"
    const val FOCUS = "focus"
    const val SETTINGS = "settings"
}
