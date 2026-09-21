package com.flowos.app.planner

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class RoutineType {
    COLLEGE_DAY,
    EXAM_DAY,
    PROJECT_DAY,
    DEEP_WORK_DAY,
    FITNESS_DAY,
    CUSTOM_DAY
}

enum class RoutineBlockType {
    WAKE,
    TRAVEL,
    COLLEGE_CLASSES,
    STUDY,
    DEEP_WORK,
    FITNESS,
    MEALS_REST,
    SLEEP,
    CUSTOM
}

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: RoutineType,
    val isDefault: Boolean = false,
    val daysOfWeek: String = "1,2,3,4,5", // 1 = Mon ... 7 = Sun
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "routine_blocks",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["routineId"])]
)
data class RoutineBlockEntity(
    @PrimaryKey val id: String,
    val routineId: String,
    val title: String,
    val blockType: RoutineBlockType,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val isFixed: Boolean = true,
    val orderIndex: Int = 0
)

@Entity(tableName = "routine_occurrences")
data class RoutineOccurrenceEntity(
    @PrimaryKey val id: String, // e.g. "routine_id-2026-09-21"
    val routineId: String,
    val dateString: String, // "YYYY-MM-DD"
    val status: String = "ACTIVE", // ACTIVE | MODIFIED | SKIPPED
    val appliedTimestamp: Long = System.currentTimeMillis()
)

data class RoutineConflict(
    val routineBlockTitle: String,
    val calendarEventTitle: String,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val resolutionSuggestion: String
)
