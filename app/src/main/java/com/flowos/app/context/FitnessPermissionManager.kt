package com.flowos.app.context

import android.content.Context
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.PermissionController

class FitnessPermissionManager(private val healthConnectManager: HealthConnectManager) {

    fun createPermissionContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }

    suspend fun checkStatus(): HealthConnectStatus {
        if (!healthConnectManager.isSdkAvailable()) {
            return HealthConnectStatus.HEALTH_CONNECT_UNAVAILABLE
        }
        return if (healthConnectManager.hasAllPermissions()) {
            HealthConnectStatus.PERMISSION_GRANTED
        } else {
            HealthConnectStatus.PERMISSION_DENIED
        }
    }
}
