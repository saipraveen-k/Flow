package com.flowos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.domain.model.Priority
import com.flowos.app.ui.HomeViewModel
import com.flowos.app.ui.components.FlowPriorityChip
import com.flowos.app.ui.components.FlowSectionHeader
import com.flowos.app.ui.components.NextActionCard
import com.flowos.app.ui.components.PulseCard
import com.flowos.app.ui.components.PulseProgress
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * AI-first dashboard. Everything shown is derived from the FlowPulse
 * WorkState engine — nothing is hardcoded to the demo scenario.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCapture: () -> Unit,
    onStartFocus: () -> Unit,
    onViewFlow: () -> Unit,
    onOpenPlan: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val workState = state.workState
    val nextAction = workState?.nextBestAction

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "FLOWOS",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                )
                Text(
                    "Good ${greetingPeriod()} 👋",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Filled.Settings, contentDescription = "Open settings")
            }
        }

        Spacer(Modifier.height(24.dp))

        // ---- Hero --------------------------------------------------------
        Text(
            "YOUR WORK, UNDERSTOOD.",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = when {
                state.loading -> "Reading your work context…"
                workState?.activeThread != null -> "You have 1 critical work thread today."
                else -> "No active work threads — capture something to begin."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(20.dp))

        // ---- Active thread (FlowPulse) -----------------------------------
        val thread = workState?.activeThread
        if (thread != null) {
            PulseCard(accent = true) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                    )
                    Spacer(Modifier.width(10.dp))
                    FlowSectionHeader("AI PULSE", Modifier.weight(1f))
                    Text(
                        "${thread.completionPercentage}% READY",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = thread.projectName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                thread.upcomingDeadlines.firstOrNull()?.let { next ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = next.deadlineLabel ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(14.dp))
                PulseProgress(progress = thread.completionPercentage / 100f)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ThreadStat("${thread.openTasks.size}", "OPEN TASKS", Modifier.weight(1f))
                    ThreadStat("${thread.blockedTasks.size}", "BLOCKED", Modifier.weight(1f))
                    ThreadStat("${thread.relatedPeople.size}", "PEOPLE", Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ---- Next best action ----------------------------------------------
        if (nextAction != null) {
            NextActionCard(
                action = nextAction,
                onStartFocus = onStartFocus,
                onViewFlow = onViewFlow,
            )
            Spacer(Modifier.height(24.dp))
        }

        // ---- Quick capture ---------------------------------------------------
        FlowSectionHeader("QUICK CAPTURE")
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickCaptureTile(Icons.Filled.CameraAlt, "IMAGE", Modifier.weight(1f), onCapture)
            QuickCaptureTile(Icons.Filled.Mic, "VOICE", Modifier.weight(1f), onCapture)
            QuickCaptureTile(Icons.Filled.Edit, "TEXT", Modifier.weight(1f), onCapture)
        }

        Spacer(Modifier.height(28.dp))

        // ---- Today -----------------------------------------------------------
        FlowSectionHeader("TODAY")
        Spacer(Modifier.height(10.dp))
        if (state.todayTasks.isEmpty()) {
            Text(
                "Nothing due today.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.todayTasks.forEach { task ->
                    TodayRow(
                        timeLabel = task.deadlineEpochMillis?.let { timeLabel(it) } ?: "",
                        title = task.title,
                        priority = Priority.from(task.priority),
                        onComplete = { viewModel.completeTask(task.id) },
                    )
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        OutlinedButton(
            onClick = onOpenPlan,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
        ) {
            Icon(Icons.Filled.Workspaces, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("OPEN PLAN", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun ThreadStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun QuickCaptureTile(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(84.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
        }
    }
}

@Composable
private fun TodayRow(
    timeLabel: String,
    title: String,
    priority: Priority,
    onComplete: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                timeLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(14.dp))
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            FlowPriorityChip(priority)
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = onComplete, contentPadding = PaddingValues(0.dp)) {
                Text("DONE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun greetingPeriod(): String {
    val hour = java.time.LocalTime.now().hour
    return when {
        hour < 12 -> "morning"
        hour < 17 -> "afternoon"
        else -> "evening"
    }
}

private fun timeLabel(millis: Long): String =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))
