package com.flowos.app.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.ui.PlanEntry
import com.flowos.app.ui.PlanViewModel
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.FlowAccent
import com.flowos.app.ui.theme.Success
import com.flowos.app.ui.theme.Warning
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
fun PlanScreen(viewModel: PlanViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070707))
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            "CALENDAR INTELLIGENCE", 
            style = MaterialTheme.typography.headlineSmall, 
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Adaptive capacity modeling.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
        )
        
        Spacer(Modifier.height(28.dp))

        if (!state.calendarConnected) {
            FlagshipCalendarAccess(onConnect = { viewModel.refreshCalendar() })
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            FlowSectionHeader("SYSTEM CAPACITY")
            Spacer(Modifier.height(12.dp))
            
            // ---- INTELLIGENCE SUMMARY ------------------------------------
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101010)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(FlowAccent.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Bolt, null, tint = FlowAccent)
                    }
                    Spacer(Modifier.width(20.dp))
                    Column {
                        Text("4.5 HOURS FREE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color.White)
                        Text("3 high-focus windows identified.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            FlowSectionHeader("ADAPTIVE TIMELINE")
            Spacer(Modifier.height(16.dp))

            state.today.forEach { entry ->
                FlagshipTimelineRow(entry)
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun FlagshipTimelineRow(entry: PlanEntry) {
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
    ) {
        Column(
            modifier = Modifier.width(60.dp).padding(top = 16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = formatTime(entry.timeMillis),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = if (entry.isEvent) Color.Gray else FlowAccent
            )
        }
        
        Spacer(Modifier.width(16.dp))
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (entry.isEvent) Color.Gray else FlowAccent))
            Box(modifier = Modifier.width(1.dp).weight(1f).background(Color.Gray.copy(alpha = 0.2f)))
        }
        
        Spacer(Modifier.width(16.dp))
        
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (entry.isEvent) Color(0xFF161616) else Color(0xFF101010)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
            modifier = Modifier.weight(1f).padding(bottom = 16.dp)
        ) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (entry.isEvent) Icons.Filled.CalendarMonth else Icons.Filled.Assignment,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (entry.isEvent) Color.Gray else FlowAccent
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (entry.isEvent) Color.Gray else Color.White
                    )
                    if (!entry.isEvent) {
                        Text("30m estimated · Critical Path", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Calendar Event", style = MaterialTheme.typography.labelSmall, color = Color.DarkGray)
                    }
                }
            }
        }
    }
}

@Composable
private fun FlagshipCalendarAccess(onConnect: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Filled.Lock, null, modifier = Modifier.size(64.dp), tint = Color.DarkGray)
        Spacer(Modifier.height(24.dp))
        Text("CALENDAR IS PROTECTED", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White)
        Spacer(Modifier.height(8.dp))
        Text(
            "FlowOS needs capacity modeling to build realistic adaptive plans.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(40.dp))
        FlowPrimaryButton(text = "UNLOCK ACCESS", onClick = onConnect)
    }
}

private fun formatTime(millis: Long): String {
    val instant = Instant.ofEpochMilli(millis)
    val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
}
