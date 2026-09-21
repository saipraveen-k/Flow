package com.flowos.app.context

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FitnessRepository(private val healthConnectManager: HealthConnectManager) {

    private val _fitnessMetrics = MutableStateFlow(FitnessMetrics(status = HealthConnectStatus.LOADING))
    val fitnessMetrics: StateFlow<FitnessMetrics> = _fitnessMetrics.asStateFlow()

    suspend fun refreshMetrics() {
        _fitnessMetrics.value = _fitnessMetrics.value.copy(status = HealthConnectStatus.LOADING)
        val metrics = healthConnectManager.readTodayMetrics()
        _fitnessMetrics.value = metrics
    }

    /**
     * Extracts protected workout time blocks from real fitness records for Planner capacity calculations.
     */
    fun getProtectedFitnessBlocks(): List<ProtectedFitnessBlock> {
        val current = _fitnessMetrics.value
        return current.exerciseSessions.map { session ->
            ProtectedFitnessBlock(
                title = session.title,
                startTimeEpochMillis = session.startTimeEpochMillis,
                endTimeEpochMillis = session.endTimeEpochMillis,
                isConfirmed = true
            )
        }
    }
}

data class ProtectedFitnessBlock(
    val title: String,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val isConfirmed: Boolean = true
)
