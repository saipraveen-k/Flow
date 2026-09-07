package com.flowos.app.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.domain.model.Priority
import com.flowos.app.ui.PlanViewModel
import com.flowos.app.ui.components.FlowSectionHeader
import com.flowos.app.ui.components.EventPreparationCard
import com.flowos.app.ui.components.PulseCard
import com.flowos.app.ui.components.ActionBundleCard
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * PLAN: the calendar + deadlines + preparation timeline. Calendar access is
 * requested contextually from here — never at app startup.
 */
@Composable
fun PlanScreen(viewModel: PlanViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) viewModel.refreshCalendar()
    }

    fun connectCalendar() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR,
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (granted) {
            viewModel.refreshCalendar()
        } else {
            calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(20.dp))
            Text("PLAN", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(4.dp))
            Text(
                "Your timeline — deadlines, events and preparation.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))

            // ---- TODAY ----------------------------------------------------
            FlowSectionHeader("TODAY")
            Spacer(Modifier.height(10.dp))
            if (state.today.isEmpty()) {
                Text(
                    "Nothing scheduled today.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.today.forEach { entry -> PlanTimelineRow(entry) }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ---- UPCOMING ---------------------------------------------------
            FlowSectionHeader("UPCOMING")
            Spacer(Modifier.height(10.dp))
            if (state.upcoming.isEmpty()) {
                Text(
                    "No upcoming entries.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                state.upcoming.forEach { day ->
                    Text(
                        day.dayLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        day.entries.forEach { entry -> PlanTimelineRow(entry) }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ---- PREPARATION --------------------------------------------------
            FlowSectionHeader("PREPARATION")
            Spacer(Modifier.height(10.dp))
            if (!state.calendarConnected) {
                PulseCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.CalendarMonth,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Connect your calendar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "FlowOS reads events locally to detect what needs preparation. Nothing is uploaded.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = ::connectCalendar,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text("CONNECT CALENDAR", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (state.preparations.isEmpty()) {
                Text(
                    "No events need preparation right now.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    state.preparations.forEach { prep ->
                        EventPreparationCard(prep = prep, onPrepare = { viewModel.prepareForEvent(prep) })
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ---- DEADLINES -----------------------------------------------------
            FlowSectionHeader("DEADLINES")
            Spacer(Modifier.height(10.dp))
            if (state.deadlines.isEmpty()) {
                Text(
                    "No tracked deadlines.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.deadlines.forEach { task ->
                        PulseCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        task.deadlineLabel ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                FlowPriorityChip(Priority.from(task.priority))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }

        // ---- Action bundle confirmation sheet ---------------------------------
        state.activeBundle?.let { bundle ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissBundle() },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(28.dp),
                title = { Text(if (state.bundleApproved) "Bundle executed" else "Approve actions") },
                text = {
                    Column {
                        ActionBundleCard(
                            bundle = bundle,
                            approved = state.bundleApproved,
                            onApprove = { viewModel.approveBundle() },
                        )
                        if (state.bundleResults.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            state.bundleResults.forEach { result ->
                                Text(
                                    result,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissBundle() }) { Text("DONE") }
                },
            )
        }
    }
}

@Composable
private fun PlanTimelineRow(entry: com.flowos.app.ui.PlanEntry) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    if (entry.isEvent) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                ),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            entry.timeMillis.let { timeLabel(it) },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(84.dp),
        )
        Text(
            entry.title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        if (entry.isEvent) {
            Text(
                "EVENT",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        } else {
            entry.priority?.let { com.flowos.app.ui.components.FlowPriorityChip(it) }
        }
    }
}

private fun timeLabel(millis: Long): String =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))
