package com.flowos.app.context

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FitnessSyncEngine(
    private val fitnessRepository: FitnessRepository,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private var syncJob: Job? = null

    fun startPeriodicSync(intervalMillis: Long = 15 * 60 * 1000L) {
        syncJob?.cancel()
        syncJob = externalScope.launch {
            while (isActive) {
                fitnessRepository.refreshMetrics()
                delay(intervalMillis)
            }
        }
    }

    fun stopPeriodicSync() {
        syncJob?.cancel()
        syncJob = null
    }
}
