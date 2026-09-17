package com.flowos.app.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.domain.model.NextBestAction
import com.flowos.app.domain.model.Priority
import com.flowos.app.pulse.FrictionRadar
import com.flowos.app.ui.HomeViewModel
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.FlowAccent
import com.flowos.app.ui.theme.Success
import com.flowos.app.ui.theme.Warning
import com.flowos.app.ui.theme.Error as FlowError
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * HOME: Flagship Attention Center.
 * Not a dashboard, but an intelligent briefing.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCapture: () -> Unit,
    onStartFocus: () -> Unit,
    onViewFlow: () -> Unit,
    onOpenPlan: () -> Unit,
    onOpenSettings: () -> Unit,
    onViewAdaptation: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val workState = state.workState
    val nextAction = workState?.nextBestAction

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        
        // ---- HEADER: Contextual Greeting ---------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "GOOD ${greetingPeriod().uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Your daily briefing.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
            // Small FlowScore indicator
            state.flowScore?.let { score ->
                FlowScorePill(score.totalScore)
            }
        }

        Spacer(Modifier.height(32.dp))

        // ---- SECTION 1: FLOWSCORE HERO -----------------------------------
        FlowScoreHero(
            score = state.flowScore?.totalScore ?: 0,
            insight = "Plan reliability improved today."
        )

        Spacer(Modifier.height(32.dp))

        // ---- SECTION 2: FLOWPULSE NBA ------------------------------------
        FlowPulseSection(
            nextAction = nextAction,
            onStartFocus = onStartFocus,
            onViewWhy = { /* Open detailed explanation */ }
        )

        Spacer(Modifier.height(32.dp))

        // ---- SECTION 3: FRICTION RADAR -----------------------------------
        if (state.frictionAlerts.isNotEmpty()) {
            FrictionRadarAlert(state.frictionAlerts.first().message, onViewAdaptation)
        } else {
            PlanOnTrackStatus()
        }

        Spacer(Modifier.height(32.dp))

        // ---- SECTION 4: TODAY'S OUTCOMES ---------------------------------
        val thread = workState?.activeThread
        if (thread != null) {
            FlowSectionHeader("ACTIVE OUTCOME")
            Spacer(Modifier.height(12.dp))
            ActiveOutcomeBrief(
                title = thread.projectName,
                progress = thread.completionPercentage,
                deadline = thread.upcomingDeadlines.firstOrNull()?.deadlineLabel ?: "8:00 PM",
                onViewGraph = onViewFlow
            )
        }

        Spacer(Modifier.height(32.dp))

        // ---- SECTION 5: TIMELINE -----------------------------------------
        FlowSectionHeader("TIMELINE")
        Spacer(Modifier.height(12.dp))
        if (state.todayTasks.isEmpty()) {
            EmptyTimelineState()
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.todayTasks.forEach { task ->
                    TimelineItem(
                        time = task.deadlineEpochMillis?.let { timeLabel(it) } ?: "Now",
                        title = task.title,
                        isCurrent = task.id == nextAction?.taskId
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        
        // ---- SECTION 6: QUICK CAPTURE ------------------------------------
        QuickCaptureHero(onCapture)

        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun FlowScorePill(score: Int) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = CircleShape,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(6.dp).background(FlowAccent, CircleShape))
            Spacer(Modifier.width(8.dp))
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

@Composable
private fun FlowScoreHero(score: Int, insight: String) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101010)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { score / 100f },
                    modifier = Modifier.size(80.dp),
                    strokeWidth = 8.dp,
                    color = FlowAccent,
                    trackColor = Color.DarkGray.copy(alpha = 0.3f),
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = score.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
            Spacer(Modifier.width(24.dp))
            Column {
                Text(
                    "FLOW SCORE",
                    style = MaterialTheme.typography.labelSmall,
                    color = FlowAccent,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    insight,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FlowPulseSection(
    nextAction: NextBestAction?,
    onStartFocus: () -> Unit,
    onViewWhy: () -> Unit
) {
    Column {
        Text(
            "WHAT DESERVES YOUR ATTENTION NOW?",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(16.dp))
        
        if (nextAction != null) {
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(2.dp, FlowAccent.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(28.dp)) {
                    Text(
                        text = nextAction.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = nextAction.reason,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FlowPrimaryButton(
                            text = "START FOCUS",
                            onClick = onStartFocus,
                            modifier = Modifier.weight(1f),
                            icon = Icons.Filled.PlayArrow
                        )
                        IconButton(
                            onClick = onViewWhy,
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                        ) {
                            Icon(Icons.Filled.HelpOutline, null, tint = FlowAccent)
                        }
                    }
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                color = Color.White.copy(alpha = 0.02f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Box(Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("NO ACTIVE THREADS", color = Color.DarkGray, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun FrictionRadarAlert(message: String, onViewAdaptation: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FlowError.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, FlowError.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Warning, null, tint = FlowError, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("PLAN AT RISK", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = FlowError)
                Text(message, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
            }
            TextButton(onClick = onViewAdaptation) {
                Text("ADAPT", color = FlowError, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun PlanOnTrackStatus() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Success.copy(alpha = 0.03f),
        border = BorderStroke(1.dp, Success.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Filled.CheckCircle, null, tint = Success, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(12.dp))
            Text("FLOW IS CLEAR", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Success, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun ActiveOutcomeBrief(title: String, progress: Int, deadline: String, onViewGraph: () -> Unit) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
        modifier = Modifier.fillMaxWidth().clickable { onViewGraph() }
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(title.uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color.White)
                Text("$progress%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = FlowAccent)
            }
            Spacer(Modifier.height(16.dp))
            FlowProgress(progress = progress / 100f)
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Timer, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text(deadline, style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TimelineItem(time: String, title: String, isCurrent: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = time,
            style = MaterialTheme.typography.labelMedium,
            color = if (isCurrent) FlowAccent else Color.Gray,
            fontWeight = FontWeight.Black,
            modifier = Modifier.width(56.dp)
        )
        Box(
            modifier = Modifier
                .size(if (isCurrent) 10.dp else 6.dp)
                .clip(CircleShape)
                .background(if (isCurrent) FlowAccent else Color.DarkGray)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isCurrent) Color.White else Color.Gray,
            fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Medium
        )
    }
}

@Composable
private fun EmptyTimelineState() {
    Text("Nothing scheduled for today.", color = Color.DarkGray, style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun QuickCaptureHero(onCapture: () -> Unit) {
    Card(
        onClick = onCapture,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Filled.Add, null, tint = FlowAccent, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Text("CAPTURE NEW GOAL", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color.White)
        }
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
