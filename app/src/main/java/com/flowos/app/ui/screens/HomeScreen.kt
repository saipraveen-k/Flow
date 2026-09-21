package com.flowos.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.domain.model.NextBestAction
import com.flowos.app.pulse.FrictionAlert
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
 * Answers: "WHAT DESERVES MY ATTENTION NOW?"
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
            .background(Color(0xFF070707))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        
        // ---- HEADER: Greeting & FlowScore --------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "FLOWOS",
                    style = MaterialTheme.typography.labelSmall,
                    color = FlowAccent,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
                Text(
                    text = "Good ${greetingPeriod()} 👋",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            }
            // SECTION 1: FLOWSCORE HERO
            state.flowScore?.let { score ->
                HeroFlowScore(score.totalScore)
            }
        }

        Spacer(Modifier.height(32.dp))

        // FLOWSCORE INSIGHT
        state.flowScore?.insight?.let { insight ->
            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AutoAwesome, null, tint = FlowAccent, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(16.dp))
                    Text(insight, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                }
            }
            Spacer(Modifier.height(32.dp))
        }

        // ---- SECTION 2: FLOWPULSE / NEXT BEST ACTION --------------------
        Text(
            text = "WHAT DESERVES MY ATTENTION NOW?",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(12.dp))
        
        if (nextAction != null) {
            NextBestActionHero(
                action = nextAction,
                onStartFocus = onStartFocus
            )
        } else {
            EmptyPulseHero(onCapture)
        }

        Spacer(Modifier.height(32.dp))

        // ---- SECTION 4: FRICTION RADAR -----------------------------------
        if (state.frictionAlerts.isNotEmpty()) {
            FlagshipFrictionRadar(state.frictionAlerts.first(), onViewAdaptation)
            Spacer(Modifier.height(32.dp))
        }

        // ---- SECTION 3: CURRENT OUTCOME ----------------------------------
        val thread = workState?.activeThread
        if (thread != null) {
            FlowSectionHeader("CURRENT OUTCOME")
            Spacer(Modifier.height(12.dp))
            OutcomeHero(
                title = thread.projectName,
                progress = thread.completionPercentage,
                deadline = thread.upcomingDeadlines.firstOrNull()?.deadlineLabel ?: "8:00 PM",
                onViewDetails = onViewFlow
            )
        }

        Spacer(Modifier.height(32.dp))

        // ---- SECTION 7: TODAY'S TIMELINE ---------------------------------
        FlowSectionHeader("TODAY'S EXECUTION")
        Spacer(Modifier.height(12.dp))
        if (state.todayTasks.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("FLOW IS CLEAR", style = MaterialTheme.typography.labelSmall, color = Success, fontWeight = FontWeight.Black)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.todayTasks.forEach { task ->
                    TimelineEntry(
                        time = task.deadlineEpochMillis?.let { timeLabel(it) } ?: "Now",
                        title = task.title,
                        isCurrent = task.id == nextAction?.taskId
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        
        // ---- SECTION 8: QUICK ACTIONS ------------------------------------
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionPill(Icons.Filled.Bolt, "SNAP", Modifier.weight(1f), {})
            QuickActionPill(Icons.Filled.Timer, "FOCUS", Modifier.weight(1f), onStartFocus)
            QuickActionPill(Icons.Filled.LaptopMac, "PC", Modifier.weight(1f), {})
        }

        Spacer(Modifier.height(100.dp)) // Padding for FAB
    }
}

@Composable
private fun HeroFlowScore(score: Int) {
    var animatedScore by remember { mutableStateOf(0) }
    LaunchedEffect(score) {
        animate(
            initialValue = 0f,
            targetValue = score.toFloat(),
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        ) { value, _ ->
            animatedScore = value.toInt()
        }
    }

    Surface(
        color = FlowAccent.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, FlowAccent.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = animatedScore.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = FlowAccent
            )
            Text(
                text = "FLOW",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = FlowAccent,
                fontSize = 8.sp
            )
        }
    }
}

@Composable
private fun NextBestActionHero(action: NextBestAction, onStartFocus: () -> Unit) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101010)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(24.dp)) {
            Text(
                text = action.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = action.reason,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
            Spacer(Modifier.height(24.dp))
            FlowPrimaryButton(
                text = "START FOCUS",
                onClick = onStartFocus,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Filled.PlayArrow
            )
        }
    }
}

@Composable
private fun OutcomeHero(title: String, progress: Int, deadline: String, onViewDetails: () -> Unit) {
    Card(
        onClick = onViewDetails,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title.uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color.White)
                Text("$progress%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = FlowAccent)
            }
            Spacer(Modifier.height(16.dp))
            FlowProgress(progress = progress / 100f)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Timer, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text(deadline, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FlagshipFrictionRadar(alert: FrictionAlert, onReplan: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FlowError.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, FlowError.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Warning, null, tint = FlowError, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("FRICTION DETECTED", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = FlowError)
                Text(alert.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
            }
            TextButton(onClick = onReplan) {
                Text("REPLAN", color = FlowError, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun TimelineEntry(time: String, title: String, isCurrent: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
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
private fun QuickActionPill(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        color = Color(0xFF161616),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = FlowAccent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.White)
        }
    }
}

@Composable
private fun EmptyPulseHero(onCapture: () -> Unit) {
    Card(
        onClick = onCapture,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.AutoAwesome, null, tint = Color.DarkGray, modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(12.dp))
                Text("NO ACTIVE FLOWS", style = MaterialTheme.typography.labelSmall, color = Color.DarkGray, fontWeight = FontWeight.Black)
            }
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
