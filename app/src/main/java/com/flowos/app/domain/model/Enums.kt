package com.flowos.app.domain.model

/** Where a capture originated from. */
enum class SourceType {
    TEXT,
    IMAGE,
    VOICE,
    DOCUMENT,
    ;

    companion object {
        fun from(raw: String?): SourceType =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: TEXT
    }
}

/** Task / capture urgency. */
enum class Priority {
    LOW,
    MEDIUM,
    HIGH,
    ;

    companion object {
        fun from(raw: String?): Priority =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: MEDIUM
    }
}

/** Coarse intent classification for a capture. */
enum class IntentType {
    TASK_ASSIGNMENT,
    MEETING,
    NOTICE,
    FOLLOW_UP,
    UNKNOWN,
    ;

    companion object {
        fun from(raw: String?): IntentType =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: UNKNOWN
    }
}

/** Lifecycle of a task. */
enum class TaskStatus {
    ACTIVE,
    DONE,
}

/** One of the four unified context signals. */
enum class LifeHub {
    PROFESSIONAL,
    PERSONAL,
    LEARNING,
    FITNESS,
    ;

    companion object {
        fun from(raw: String?): LifeHub =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: PROFESSIONAL
    }
}

/** Verification state for outcomes and evidence-bearing tasks. */
enum class VerificationState {
    PLANNED,
    IN_PROGRESS,
    COMPLETED,
    EVIDENCE_ATTACHED,
    VERIFIED,
}
