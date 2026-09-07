package com.flowos.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.ui.FlowViewModel
import com.flowos.app.ui.components.FlowEmptyState
import com.flowos.app.ui.components.FlowPrimaryButton
import com.flowos.app.ui.components.FlowSectionHeader
import com.flowos.app.ui.components.FlowNodeCard
import com.flowos.app.ui.components.PulseCard
import com.flowos.app.ui.components.PulseProgress

/**
 * FLOW: the signature screen. Renders the active work thread as a vertical
 * dependency chain with staggered node reveal. Data is fully derived —
 * nodes show task, priority, deadline and completion.
 */
@Composable
fun FlowScreen(
    viewModel: FlowViewModel,
    onStartFocus: () -> Unit,
    onCapture: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Text("FLOW", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(4.dp))
        Text(
            "Dependency-aware view of your active work.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))

        if (state.isEmpty || state.nodes.isEmpty()) {
            FlowEmptyState(
                title = "No active flow",
                description = "Capture something or complete tasks to shape your dependency graph.",
            )
            Spacer(Modifier.height(16.dp))
            FlowPrimaryButton(text = "CAPTURE", onClick = onCapture, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(40.dp))
            return@Column
        }

        PulseCard(accent = true) {
            FlowSectionHeader("PROJECT")
            Text(
                state.projectName ?: "Untitled",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(10.dp))
            PulseProgress(
                progress = if (state.nodes.isEmpty()) 0f else {
                    val done = state.nodes.count {
                        it.status == com.flowos.app.domain.model.TaskStatus.DONE.name
                    }
                    done.toFloat() / state.nodes.size
                },
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "${state.nodes.size} tasks · ${state.deadlineCount} deadlines · ${state.dependencyCount} dependency links",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(28.dp))
        FlowSectionHeader("DEPENDENCY CHAIN")
        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            state.nodes.forEachIndexed { index, task ->
                FlowNodeCard(
                    index = index,
                    task = task,
                    isLast = index == state.nodes.lastIndex,
                )
            }
        }

        Spacer(Modifier.height(28.dp))
        FlowPrimaryButton(
            text = "START FOCUS",
            onClick = onStartFocus,
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Filled.PlayArrow,
        )
        Spacer(Modifier.height(40.dp))
    }
}
