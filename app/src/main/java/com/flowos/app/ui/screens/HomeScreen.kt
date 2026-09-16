package com.flowos.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.data.local.FlowScoreEntity
import com.flowos.app.domain.model.Priority
import com.flowos.app.pulse.FrictionRadar
import com.flowos.app.ui.HomeViewModel
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.Success
import com.flowos.app.ui.theme.Warning
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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
        
        // ---- TOP: Greeting & FlowScore -----------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Good ${greetingPeriod()} 👋",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "What deserves my attention now?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                )
            }
            state.flowScore?.let { score ->
                FlowScoreHero(score.totalScore)
            }
        }

        Spacer(Modifier.height(24.dp))

        // ---- FLOWPULSE: Next Best Action ---------------------------------
        if (nextAction != null) {
            NextActionCard(
                action = nextAction,
                onStartFocus = onStartFocus,
                onViewFlow = onViewFlow,
            )
        } else {
            EmptyPulseCard(onCapture)
        }

        Spacer(Modifier.height(24.dp))

        // ---- FRICTION RADAR ----------------------------------------------
        if (state.frictionAlerts.isNotEmpty()) {
            FrictionRadarCard(state.frictionAlerts)
            Spacer(Modifier.height(24.dp))
        } else {
            PlanOnTrackIndicator()
            Spacer(Modifier.height(24.dp))
        }

        // ---- CURRENT OUTCOME ---------------------------------------------
        val thread = workState?.activeThread
        if (thread != null) {
            FlowSectionHeader("CURRENT OUTCOME")
            Spacer(Modifier.height(12.dp))
            CurrentOutcomeCard(
                title = thread.projectName,
                progress = thread.completionPercentage,
                deadline = thread.upcomingDeadlines.firstOrNull()?.deadlineLabel ?: "No deadline",
                onViewGraph = onViewFlow
            )
            Spacer(Modifier.height(28.dp))
        }

        // ---- TODAY TIMELINE ----------------------------------------------
        FlowSectionHeader("TODAY")
        Spacer(Modifier.height(12.dp))
        if (state.todayTasks.isEmpty()) {
            Text(
                "Nothing scheduled today.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.todayTasks.forEach { task ->
                    TodayTimelineRow(
                        time = task.deadlineEpochMillis?.let { timeLabel(it) } ?: "Now",
                        title = task.title,
                        isCurrent = task.id == nextAction?.taskId
                    )
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun FlowScoreHero(score: Int) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "FLOW",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun EmptyPulseCard(onCapture: () -> Unit) {
    PulseCard(accent = true, onClick = onCapture) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(12.dp))
            Text("Capture an outcome to begin", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("FlowOS will guide your attention.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PlanOnTrackIndicator() {
    Surface(
        color = Success.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.CheckCircle, null, tint = Success, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text("PLAN IS ON TRACK", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Success)
        }
    }
}

@Composable
private fun CurrentOutcomeCard(
    title: String,
    progress: Int,
    deadline: String,
    onViewGraph: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth().clickable { onViewGraph() }
    ) {
        Column(Modifier.padding(24.dp)) {
            Text(title.uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Spacer(Modifier.height(16.dp))
            FlowProgress(progress = progress / 100f)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("DEADLINE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(deadline, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onViewGraph) {
                    Text("VIEW WORK GRAPH", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Icon(Icons.Filled.ChevronRight, null)
                }
            }
        }
    }
}

@Composable
private fun FrictionRadarCard(alerts: List<FrictionRadar.FrictionAlert>) {
    val critical = alerts.filter { it.severity == FrictionRadar.Severity.CRITICAL }
    val warningColor = if (critical.isNotEmpty()) MaterialTheme.colorScheme.error else Warning

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = warningColor.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, warningColor.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Warning, null, tint = warningColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                FlowSectionHeader("PLAN AT RISK", Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            alerts.forEach { alert ->
                Text(alert.message, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { /* Navigate to Replanning */ },
                colors = ButtonDefaults.buttonColors(containerColor = warningColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("VIEW ADAPTATION", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TodayTimelineRow(
    time: String,
    title: String,
    isCurrent: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = time,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(64.dp)
        )
        Box(
            modifier = Modifier
                .size(if (isCurrent) 12.dp else 8.dp)
                .clip(CircleShape)
                .background(if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            style = if (isCurrent) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            color = if (isCurrent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun greetingPeriod(): String {
    val hour = LocalTime.now().hour
    return when {
        hour < 12 -> "morning"
        hour < 17 -> "afternoon"
        else -> "evening"
    }
}

private fun timeLabel(millis: Long): String =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm", Locale.US))
