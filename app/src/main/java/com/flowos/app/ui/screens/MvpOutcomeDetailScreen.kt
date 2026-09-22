package com.flowos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.ui.OutcomeDetailViewModel
import com.flowos.app.ui.components.FlowPrimaryButton
import com.flowos.app.ui.theme.DesignTokens

@Composable
fun MvpOutcomeDetailScreen(viewModel: OutcomeDetailViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }
    LaunchedEffect(state.deleted) { if (state.deleted) onBack() }
    val outcome = state.outcome
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(DesignTokens.Spacing.Large)) {
        Row { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }; Text("OUTCOME", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black) }
        Spacer(Modifier.height(32.dp))
        if (outcome == null) {
            Text("Outcome not found.")
        } else {
            Text(outcome.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            if (outcome.description.isNotBlank()) { Spacer(Modifier.height(12.dp)); Text(outcome.description) }
            Spacer(Modifier.height(16.dp))
            Text("STATUS: ${outcome.status}", style = MaterialTheme.typography.labelLarge)
            outcome.deadlineEpochMillis?.let { Spacer(Modifier.height(8.dp)); Text("Deadline saved") }
            Spacer(Modifier.height(32.dp))
            if (outcome.status != "COMPLETED") FlowPrimaryButton("COMPLETE", viewModel::complete, Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth()) { Text("DELETE") }
        }
    }
    if (confirmDelete) AlertDialog(
        onDismissRequest = { confirmDelete = false },
        title = { Text("Delete outcome?") }, text = { Text("This cannot be undone.") },
        confirmButton = { TextButton(onClick = { confirmDelete = false; viewModel.delete() }) { Text("DELETE") } },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("CANCEL") } },
    )
}
