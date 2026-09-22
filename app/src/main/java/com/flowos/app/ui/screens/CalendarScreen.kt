package com.flowos.app.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.ui.PlanEntry
import com.flowos.app.ui.PlanViewModel
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * CALENDAR INTELLIGENCE: Flagship capacity planner.
 * Visualizes available work windows and protected time.
 */
@Composable
fun CalendarScreen(viewModel: PlanViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.refreshCalendar()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = DesignTokens.Spacing.Large),
    ) {
        Spacer(Modifier.height(DesignTokens.Spacing.Large))
        Text(
            "CALENDAR INTELLIGENCE", 
            style = MaterialTheme.typography.headlineSmall, 
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(DesignTokens.Spacing.Tiny))
        Text(
            "Adaptive capacity modeling.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        
        Spacer(Modifier.height(DesignTokens.Spacing.Huge))

        if (!state.calendarConnected) {
            FlagshipCalendarAccess(
                onConnect = { launcher.launch(Manifest.permission.READ_CALENDAR) },
                onOpenSettings = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            )
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            FlowSectionHeader("SYSTEM CAPACITY")
            Spacer(Modifier.height(DesignTokens.Spacing.Medium))
            
            // ---- INTELLIGENCE SUMMARY ------------------------------------
            FlowCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(FlowAccent.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Bolt, null, tint = FlowAccent)
                    }
                    Spacer(Modifier.width(DesignTokens.Spacing.Large))
                    Column {
                        val hours = state.availableCapacityMinutes / 60
                        val mins = state.availableCapacityMinutes % 60
                        val capacityLabel = if (hours > 0) "${hours}h ${mins}m FREE" else "${mins}m FREE"
                        
                        Text(capacityLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                        Text("Available focus capacity today.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(DesignTokens.Spacing.Huge))
            FlowSectionHeader("ADAPTIVE TIMELINE")
            Spacer(Modifier.height(DesignTokens.Spacing.Medium))

            if (state.today.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(DesignTokens.Spacing.Huge), contentAlignment = Alignment.Center) {
                    Text("NO EVENTS SCHEDULED", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Black)
                }
            } else {
                state.today.forEach { entry ->
                    FlagshipTimelineRow(entry)
                }
            }
            
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun FlagshipTimelineRow(entry: PlanEntry) {
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
    ) {
        Column(
            modifier = Modifier.width(64.dp).padding(top = DesignTokens.Spacing.Medium),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = formatTime(entry.timeMillis),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = if (entry.isEvent) MaterialTheme.colorScheme.onSurfaceVariant else FlowAccent
            )
        }
        
        Spacer(Modifier.width(DesignTokens.Spacing.Medium))
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (entry.isEvent) MaterialTheme.colorScheme.outline else FlowAccent))
            Box(modifier = Modifier.width(1.dp).weight(1f).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)))
        }
        
        Spacer(Modifier.width(DesignTokens.Spacing.Medium))
        
        FlowCard(
            modifier = Modifier.weight(1f).padding(bottom = DesignTokens.Spacing.Medium),
            backgroundColor = if (entry.isEvent) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (entry.isEvent) Icons.Filled.CalendarMonth else Icons.Filled.Assignment,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (entry.isEvent) MaterialTheme.colorScheme.onSurfaceVariant else FlowAccent
                )
                Spacer(Modifier.width(DesignTokens.Spacing.Medium))
                Column {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (entry.isEvent) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                    if (!entry.isEvent) {
                        Text("30m estimated · Critical Path", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Calendar Event", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}

@Composable
private fun FlagshipCalendarAccess(onConnect: () -> Unit, onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Filled.Lock, null, modifier = Modifier.size(DesignTokens.Spacing.Hero + 24.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.height(DesignTokens.Spacing.Large))
        Text("CALENDAR IS PROTECTED", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(DesignTokens.Spacing.Small))
        Text(
            "FlowOS needs capacity modeling to build realistic adaptive plans.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = DesignTokens.Spacing.Large)
        )
        Spacer(Modifier.height(DesignTokens.Spacing.Huge))
        FlowPrimaryButton(text = "UNLOCK ACCESS", onClick = onConnect)
        Spacer(Modifier.height(DesignTokens.Spacing.Medium))
        TextButton(onClick = onOpenSettings) {
            Text("OPEN SETTINGS", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatTime(millis: Long): String {
    val instant = Instant.ofEpochMilli(millis)
    val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
}
