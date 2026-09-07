package com.flowos.app.pulse

/** One directed "from must finish before to" edge, derived from persistence. */
data class TaskDependencyEdge(
    val fromTaskId: String,
    val toTaskId: String,
    val reason: String = "",
)
