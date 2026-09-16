package com.flowos.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.ui.ActivityViewModel
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.Success

@Composable
fun OutcomeListScreen(
    viewModel: ActivityViewModel, // Reusing for now
    onOutcomeClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Text("OUTCOMES", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(4.dp))
        Text(
            "Manage your measurable results.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(Modifier.height(24.dp))

        FlowSectionHeader("ACTIVE")
        Spacer(Modifier.height(12.dp))
        
        uiState.project?.let { project ->
            OutcomeCard(
                title = project.name,
                progress = project.progressPercent,
                status = "ON TRACK",
                onClick = { onOutcomeClick(project.id) }
            )
        } ?: FlowEmptyState(title = "No active outcomes", description = "Capture a goal to begin.")

        Spacer(Modifier.height(32.dp))
        FlowSectionHeader("VERIFIED")
        Spacer(Modifier.height(12.dp))
        Text(
            "Completed and verified outcomes appear here.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun OutcomeCard(
    title: String,
    progress: Int,
    status: String,
    onClick: () -> Unit
) {
    PulseCard(onClick = onClick) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                StatusBadge(status)
            }
            Spacer(Modifier.height(16.dp))
            FlowProgress(progress = progress / 100f)
            Spacer(Modifier.height(8.dp))
            Text("${progress}% complete", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    Surface(
        color = Success.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Success,
            fontWeight = FontWeight.Black
        )
    }
}
