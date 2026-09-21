package com.flowos.app.context

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Length
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

enum class HealthConnectStatus {
    PERMISSION_GRANTED,
    PERMISSION_DENIED,
    HEALTH_CONNECT_UNAVAILABLE,
    NO_DATA,
    LOADING,
    ERROR,
    STALE_DATA,
    SUCCESS
}

data class FitnessMetrics(
    val steps: Long? = null,
    val activeCalories: Double? = null,
    val distanceMeters: Double? = null,
    val workoutDurationMinutes: Long? = null,
    val exerciseSessions: List<WorkoutSession> = emptyList(),
    val status: HealthConnectStatus = HealthConnectStatus.NO_DATA,
    val errorMessage: String? = null,
    val lastSyncedTimestamp: Long = 0L
)

data class WorkoutSession(
    val title: String,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val exerciseType: Int
)

class HealthConnectManager(private val context: Context) {

    val requiredPermissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class)
    )

    fun isSdkAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }

    private fun getClient(): HealthConnectClient? {
        return if (isSdkAvailable()) HealthConnectClient.getOrCreate(context) else null
    }

    suspend fun hasAllPermissions(): Boolean {
        val client = getClient() ?: return false
        val granted = client.permissionController.getGrantedPermissions()
        return granted.containsAll(requiredPermissions)
    }

    suspend fun readTodayMetrics(): FitnessMetrics {
        val client = getClient()
        if (client == null) {
            return FitnessMetrics(status = HealthConnectStatus.HEALTH_CONNECT_UNAVAILABLE)
        }

        if (!hasAllPermissions()) {
            return FitnessMetrics(status = HealthConnectStatus.PERMISSION_DENIED)
        }

        return try {
            val now = Instant.now()
            val startOfDay = now.atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()
            val timeRange = TimeRangeFilter.between(startOfDay, now)

            // Aggregate steps using Health Connect aggregate API to handle overlapping sources safely
            val stepAggregateResult = client.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = timeRange
                )
            )
            val stepsCount = stepAggregateResult[StepsRecord.COUNT_TOTAL]

            // Aggregate active calories
            val calorieAggregateResult = client.aggregate(
                AggregateRequest(
                    metrics = setOf(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL),
                    timeRangeFilter = timeRange
                )
            )
            val calories = calorieAggregateResult[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories

            // Aggregate distance
            val distanceAggregateResult = client.aggregate(
                AggregateRequest(
                    metrics = setOf(DistanceRecord.DISTANCE_TOTAL),
                    timeRangeFilter = timeRange
                )
            )
            val distanceM = distanceAggregateResult[DistanceRecord.DISTANCE_TOTAL]?.inMeters

            // Read workout sessions
            val exerciseResponse = client.readRecords(
                ReadRecordsRequest(
                    recordType = ExerciseSessionRecord::class,
                    timeRangeFilter = timeRange
                )
            )

            val sessions = exerciseResponse.records.map { record ->
                val durationMin = ChronoUnit.MINUTES.between(record.startTime, record.endTime)
                WorkoutSession(
                    title = record.title ?: "Workout Session",
                    startTimeEpochMillis = record.startTime.toEpochMilli(),
                    endTimeEpochMillis = record.endTime.toEpochMilli(),
                    exerciseType = record.exerciseType
                )
            }

            val totalWorkoutDuration = sessions.sumOf { (it.endTimeEpochMillis - it.startTimeEpochMillis) / 60_000 }

            val hasAnyData = stepsCount != null || calories != null || distanceM != null || sessions.isNotEmpty()
            val finalStatus = if (hasAnyData) HealthConnectStatus.SUCCESS else HealthConnectStatus.NO_DATA

            FitnessMetrics(
                steps = stepsCount,
                activeCalories = calories,
                distanceMeters = distanceM,
                workoutDurationMinutes = if (totalWorkoutDuration > 0) totalWorkoutDuration else null,
                exerciseSessions = sessions,
                status = finalStatus,
                lastSyncedTimestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            FitnessMetrics(
                status = HealthConnectStatus.ERROR,
                errorMessage = e.localizedMessage ?: "Failed to read Health Connect records"
            )
        }
    }
}
