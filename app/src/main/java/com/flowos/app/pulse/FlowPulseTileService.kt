package com.flowos.app.pulse

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import com.flowos.app.MainActivity
import com.flowos.app.FlowOSApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FlowPulseTileService : TileService() {
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    private fun updateTile() {
        val container = (application as FlowOSApplication).container
        serviceScope.launch {
            val tasks = container.repository.observeAllTasks().first()
            val projects = container.repository.observeProjectsOnce()
            val dependencies = container.repository.getAllDependencies().map { 
                TaskDependencyEdge(it.fromTaskId, it.toTaskId, it.reason)
            }
            
            val workState = container.workStateEngine.compute(
                projects = projects,
                tasks = tasks,
                dependencies = dependencies,
                calendarEvents = emptyList(),
                nowMillis = System.currentTimeMillis()
            )
            
            val nextAction = workState.nextBestAction
            val tile = qsTile
            if (nextAction != null) {
                tile.label = "Next: ${nextAction.title}"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = nextAction.reason
                }
            } else {
                tile.label = "Flow Pulse"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = "No active tasks"
                }
            }
            tile.updateTile()
        }
    }

    override fun onClick() {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = "com.flowos.app.ACTION_FLOW_PULSE"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
