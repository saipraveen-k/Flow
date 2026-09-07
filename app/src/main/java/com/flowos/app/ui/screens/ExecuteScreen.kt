package com.flowos.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.ui.ExecuteViewModel
import com.flowos.app.ui.components.*

@Composable
fun ExecuteScreen(
    viewModel: ExecuteViewModel,
    onDone: () -> Unit,
    onEdit: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { viewModel.execute() }

    /** POST_NOTIFICATIONS is requested only at the moment execution needs it. */
    fun executeWithNotificationSafety() {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.execute()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.prepare()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Text("EXECUTION", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (state.finished) "All steps completed" else "Ready to execute?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(32.dp))

        if (state.finished) {
            FlowEmptyState(
                title = "Workflow Complete",
                description = "FlowOS has registered all tasks and scheduled your reminders.",
                icon = Icons.Filled.CheckCircle
            )
            Spacer(Modifier.height(32.dp))
            FlowPrimaryButton(text = "DONE", onClick = onDone, modifier = Modifier.fillMaxWidth())
        } else {
            FlowSectionHeader("SYSTEM ACTIONS")
            Spacer(Modifier.height(12.dp))
            
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(24.dp)) {
                    state.actions.forEach { action ->
                        ActionRow(
                            title = action.title,
                            detail = action.detail,
                            done = state.completedKinds.contains(action.kind)
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            if (state.failures.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("SOME ACTIONS FAILED", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        state.failures.forEach { error ->
                            Text(error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            FlowPrimaryButton(
                text = if (state.running) "EXECUTING..." else "EXECUTE ALL",
                onClick = ::executeWithNotificationSafety,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Filled.PlayArrow,
                enabled = !state.running
            )
            
            Spacer(Modifier.height(12.dp))
            FlowSecondaryButton(
                text = "REVISE WORKFLOW",
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.running
            )
        }
        
        Spacer(Modifier.height(40.dp))
    }
}
