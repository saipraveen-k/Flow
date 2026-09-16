package com.flowos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.ui.PlanEntry
import com.flowos.app.ui.PlanViewModel
import com.flowos.app.ui.components.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun PlanScreen(viewModel: PlanViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Text("CALENDAR", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(4.dp))
        Text(
            "Intelligence-aware capacity view.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        
        Spacer(Modifier.height(24.dp))

        if (!state.calendarConnected) {
            CalendarAccessState(onConnect = { viewModel.refreshCalendar() })
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            FlowSectionHeader("TODAY'S CAPACITY")
            Spacer(Modifier.height(12.dp))
            
            // ---- CAPACITY BAR --------------------------------------------
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Bolt, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text("4.5 HOURS FREE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("FlowOS identified 3 high-focus windows today.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(32.dp))
            FlowSectionHeader("TIMELINE")
            Spacer(Modifier.height(16.dp))

            state.today.forEach { entry ->
                TimelineEntryRow(entry)
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun TimelineEntryRow(entry: PlanEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.width(64.dp)) {
            Text(
                text = formatTime(entry.timeMillis),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = if (entry.isEvent) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(Modifier.width(16.dp))
        
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (entry.isEvent) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.weight(1f)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (entry.isEvent) Icons.Filled.CalendarToday else Icons.Filled.Assignment,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (entry.isEvent) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (entry.isEvent) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                    if (!entry.isEvent) {
                        Text("30 min estimated", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarAccessState(onConnect: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Filled.CalendarMonth, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.height(16.dp))
        Text("Calendar access needed", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(8.dp))
        Text(
            "FlowOS needs to understand your available capacity to build a realistic plan.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        FlowPrimaryButton(text = "ALLOW ACCESS", onClick = onConnect)
    }
}

private fun formatTime(millis: Long): String {
    val instant = Instant.ofEpochMilli(millis)
    val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
}
