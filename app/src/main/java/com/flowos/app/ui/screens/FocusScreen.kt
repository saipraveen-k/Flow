package com.flowos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.ui.FocusViewModel
import com.flowos.app.ui.components.FlowEmptyState
import com.flowos.app.ui.components.FlowPriorityChip
import com.flowos.app.ui.components.PulseProgress

/**
 * FOCUS MODE: one task at a time from the active thread's dependency chain.
 * Visually minimal by design — no dashboard, no clutter. SKIP advances
 * without marking anything complete.
 */
@Composable
fun FocusScreen(
    viewModel: FocusViewModel,
    onExit: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                state.projectName?.uppercase() ?: "FOCUS",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onExit) {
                Icon(Icons.Filled.Close, contentDescription = "Exit focus mode", Modifier.size(18.dp))
            }
        }

        if (state.finished || state.currentTask == null) {
            Spacer(Modifier.height(48.dp))
            FlowEmptyState(
                title = if (state.totalSteps > 0) "Thread complete" else "Nothing to focus on",
                description = if (state.totalSteps > 0) {
                    "Every step of this thread is done or skipped."
                } else {
                    "Capture something or plan your day to fill your focus queue."
                },
                icon = Icons.Filled.Check,
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onExit,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text("DONE", fontWeight = FontWeight.Bold)
            }
            return@Column
        }

        val task = state.currentTask ?: return@Column
        Spacer(Modifier.height(28.dp))
        Text(
            "STEP ${state.stepIndex} OF ${state.totalSteps}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(10.dp))
        PulseProgress(progress = state.stepIndex.toFloat() / state.totalSteps.coerceAtLeast(1))

        Spacer(Modifier.height(32.dp))
        Text(
            task.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        if (task.description.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(
                task.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            FlowPriorityChip(com.flowos.app.domain.model.Priority.from(task.priority))
            task.deadlineLabel?.let {
                Spacer(Modifier.width(10.dp))
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Spacer(Modifier.height(36.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = { viewModel.openCurrent() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Filled.OpenInNew, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("OPEN", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.completeCurrent()
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Icon(Icons.Filled.Check, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("COMPLETE", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = { viewModel.skipCurrent() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text("SKIP", fontWeight = FontWeight.Bold)
        }

        state.statusMessage?.let { message ->
            Spacer(Modifier.height(16.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(32.dp))
        Text(
            "DEPENDENCY CHAIN",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(10.dp))
        state.chainTitles.forEachIndexed { index, title ->
            Row(
                Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                index + 1 < state.stepIndex -> MaterialTheme.colorScheme.primary
                                index + 1 == state.stepIndex -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            },
                        ),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (index + 1 == state.stepIndex) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}
