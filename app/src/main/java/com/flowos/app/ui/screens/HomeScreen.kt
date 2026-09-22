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
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.crossdevice.RealCrossDeviceConnectionState
import com.flowos.app.data.local.TaskEntity
import com.flowos.app.domain.model.NextBestAction
import com.flowos.app.domain.model.WorkState
import com.flowos.app.pulse.FrictionAlert
import com.flowos.app.ui.HomeViewModel
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.*
import com.flowos.app.ui.theme.Error as FlowError
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    widthSizeClass: WindowWidthSizeClass,
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

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val maxWidth = FlowAdaptive.maxContentWidth(widthSizeClass)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (widthSizeClass == WindowWidthSizeClass.Compact) 20.dp else 40.dp)
                .align(Alignment.TopCenter)
                .then(if (maxWidth != Dp.Unspecified) Modifier.width(maxWidth) else Modifier),
        ) {
            Spacer(Modifier.height(DesignTokens.Spacing.Large))
            
            // ---- HEADER ----
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
                        fontWeight = FontWeight.Black
                    )
                }
                state.flowScore?.let { score ->
                    HeroFlowScore(score.totalScore)
                }
            }

            Spacer(Modifier.height(DesignTokens.Spacing.Huge))

            // ---- CONTENT GRID / LAYOUT ----
            if (widthSizeClass == WindowWidthSizeClass.Expanded) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        NextBestActionSection(nextAction, onStartFocus, onCapture)
                        Spacer(Modifier.height(24.dp))
                        OfficeKitSection { onOpenSettings() }
                        Spacer(Modifier.height(24.dp))
                        FrictionSection(state.frictionAlerts, onViewAdaptation)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        OutcomeSection(workState, onViewFlow)
                        Spacer(Modifier.height(24.dp))
                        TimelineSection(state.todayTasks, nextAction)
                    }
                }
            } else {
                NextBestActionSection(nextAction, onStartFocus, onCapture)
                Spacer(Modifier.height(32.dp))
                OfficeKitSection { onOpenSettings() }
                Spacer(Modifier.height(32.dp))
                FrictionSection(state.frictionAlerts, onViewAdaptation)
                Spacer(Modifier.height(32.dp))
                OutcomeSection(workState, onViewFlow)
                Spacer(Modifier.height(32.dp))
                TimelineSection(state.todayTasks, nextAction)
            }

            Spacer(Modifier.height(32.dp))
            
            // ---- QUICK ACTIONS ----
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionPill(Icons.Filled.Bolt, "SNAP", Modifier.weight(1f), {})
                QuickActionPill(Icons.Filled.Timer, "FOCUS", Modifier.weight(1f), onStartFocus)
                QuickActionPill(Icons.Filled.LaptopMac, "PC", Modifier.weight(1f), {})
            }

            Spacer(Modifier.height(120.dp))
        }
    }
}

@Composable
private fun NextBestActionSection(nextAction: NextBestAction?, onStartFocus: () -> Unit, onCapture: () -> Unit) {
    Column {
        FlowSectionHeader("WHAT DESERVES ATTENTION NOW?")
        if (nextAction != null) {
            NextBestActionHero(action = nextAction, onStartFocus = onStartFocus)
        } else {
            EmptyPulseHero(onCapture)
        }
    }
}

@Composable
private fun FrictionSection(alerts: List<FrictionAlert>, onViewAdaptation: () -> Unit) {
    if (alerts.isNotEmpty()) {
        Column {
            FlowSectionHeader("FRICTION DETECTED")
            FlagshipFrictionRadar(alerts.first(), onViewAdaptation)
        }
    }
}

@Composable
private fun OutcomeSection(workState: WorkState?, onViewFlow: () -> Unit) {
    val thread = workState?.activeThread
    if (thread != null) {
        Column {
            FlowSectionHeader("CURRENT OUTCOME")
            OutcomeHero(
                title = thread.projectName,
                progress = thread.completionPercentage,
                deadline = thread.upcomingDeadlines.firstOrNull()?.deadlineLabel ?: "8:00 PM",
                onViewDetails = onViewFlow
            )
        }
    }
}

@Composable
private fun TimelineSection(todayTasks: List<TaskEntity>, nextAction: NextBestAction?) {
    Column {
        FlowSectionHeader("TODAY'S EXECUTION")
        if (todayTasks.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("FLOW IS CLEAR", style = MaterialTheme.typography.labelSmall, color = Success, fontWeight = FontWeight.Black)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                todayTasks.forEach { task ->
                    TimelineEntry(
                        time = task.deadlineEpochMillis?.let { timeLabel(it) } ?: "Now",
                        title = task.title,
                        isCurrent = task.id == nextAction?.taskId
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroFlowScore(score: Int) {
    var animatedScore by remember { mutableStateOf(0) }
    LaunchedEffect(score) {
        animate(
            initialValue = 0f,
            targetValue = score.toFloat(),
            animationSpec = tween(durationMillis = DesignTokens.Animation.Normal * 3, easing = DesignTokens.Animation.DefaultEasing)
        ) { value, _ -> animatedScore = value.toInt() }
    }

    Surface(
        color = FlowAccent.copy(alpha = 0.1f),
        shape = RoundedCornerShape(DesignTokens.Shapes.Medium),
        border = BorderStroke(1.dp, FlowAccent.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = animatedScore.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = FlowAccent)
            Text(text = "FLOW", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = FlowAccent, fontSize = 8.sp)
        }
    }
}

@Composable
private fun NextBestActionHero(action: NextBestAction, onStartFocus: () -> Unit) {
    FlowCard {
        Text(text = action.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(8.dp))
        Text(text = action.reason, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        FlowPrimaryButton(text = "START FOCUS", onClick = onStartFocus, modifier = Modifier.fillMaxWidth(), icon = Icons.Filled.PlayArrow)
    }
}

@Composable
private fun OutcomeHero(title: String, progress: Int, deadline: String, onViewDetails: () -> Unit) {
    FlowCard(onClick = onViewDetails, backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title.uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            Text("$progress%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = FlowAccent)
        }
        Spacer(Modifier.height(16.dp))
        FlowProgress(progress = progress / 100f)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Timer, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(8.dp))
            Text(deadline, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FlagshipFrictionRadar(alert: FrictionAlert, onReplan: () -> Unit) {
    Card(
        shape = RoundedCornerShape(DesignTokens.Shapes.Medium),
        colors = CardDefaults.cardColors(containerColor = FlowError.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, FlowError.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Warning, null, tint = FlowError, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("FRICTION DETECTED", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = FlowError)
                Text(alert.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
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
        Text(text = time, style = MaterialTheme.typography.labelMedium, color = if (isCurrent) FlowAccent else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Black, modifier = Modifier.width(56.dp))
        Box(modifier = Modifier.size(if (isCurrent) 10.dp else 6.dp).clip(CircleShape).background(if (isCurrent) FlowAccent else MaterialTheme.colorScheme.outline))
        Spacer(Modifier.width(16.dp))
        Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Medium)
    }
}

@Composable
private fun QuickActionPill(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(DesignTokens.Shapes.Medium),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = FlowAccent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun EmptyPulseHero(onCapture: () -> Unit) {
    FlowCard(onClick = onCapture, backgroundColor = Color.Transparent) {
        Box(Modifier.padding(DesignTokens.Spacing.Large), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.AutoAwesome, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(12.dp))
                Text("NO ACTIVE FLOWS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun OfficeKitSection(onOpen: () -> Unit) {
    val isConnected = RealCrossDeviceConnectionState.isConnected
    Column {
        FlowSectionHeader("OFFICE KIT")
        FlowCard(
            onClick = onOpen,
            backgroundColor = if (isConnected) Success.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) Success else Warning)
                )
                Spacer(Modifier.width(DesignTokens.Spacing.Medium))
                Text(
                    text = if (isConnected) "PC CONNECTED" else "PC DISCONNECTED",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = if (isConnected) Success else Warning
                )
                Spacer(Modifier.weight(1f))
                Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
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
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm", Locale.US))
