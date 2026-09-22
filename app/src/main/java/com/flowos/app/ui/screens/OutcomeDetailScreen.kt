package com.flowos.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.domain.model.Priority
import com.flowos.app.domain.model.TaskStatus
import com.flowos.app.ui.FlowUiState
import com.flowos.app.ui.FlowViewModel
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.*

/**
 * OUTCOME DETAIL: Flagship Command Center.
 * 5-Tab Structure: Overview, Work Graph, Timeline, Proof, Activity.
 */
@Composable
fun OutcomeDetailScreen(
    viewModel: FlowViewModel,
    widthSizeClass: WindowWidthSizeClass,
    onStartFocus: () -> Unit,
    onCapture: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val maxWidth = FlowAdaptive.maxContentWidth(widthSizeClass)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = DesignTokens.Spacing.Large)
                .align(Alignment.TopCenter)
                .then(if (maxWidth != Dp.Unspecified) Modifier.width(maxWidth) else Modifier),
        ) {
            Spacer(Modifier.height(DesignTokens.Spacing.Large))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) }
                Text(
                    text = state.projectName?.uppercase() ?: "OUTCOME",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
            
            Spacer(Modifier.height(DesignTokens.Spacing.Medium))
            
            // ---- FLAGSHIP SEGMENTED NAVIGATION ----
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = FlowAccent
                    )
                },
                edgePadding = 0.dp
            ) {
                FlagshipTab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = "OVERVIEW")
                FlagshipTab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = "WORK GRAPH")
                FlagshipTab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = "TIMELINE")
                FlagshipTab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = "PROOF")
                FlagshipTab(selected = selectedTab == 4, onClick = { selectedTab = 4 }, text = "ACTIVITY")
            }

            Spacer(Modifier.height(DesignTokens.Spacing.Large))

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "detailTransition"
                ) { tab ->
                    when (tab) {
                        0 -> OutcomeOverviewTab(state, onStartFocus)
                        1 -> OutcomeGraphTab(state)
                        2 -> OutcomeTimelineTab(state)
                        3 -> OutcomeProofTab(state)
                        4 -> OutcomeActivityTab(state)
                    }
                }
                Spacer(Modifier.height(DesignTokens.Spacing.Hero))
            }
        }
    }
}

@Composable
private fun FlagshipTab(selected: Boolean, onClick: () -> Unit, text: String) {
    Tab(
        selected = selected,
        onClick = onClick,
        text = {
            Text(
                text = text, 
                style = MaterialTheme.typography.labelSmall, 
                fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }
    )
}

@Composable
private fun OutcomeOverviewTab(state: FlowUiState, onStartFocus: () -> Unit) {
    Column {
        FlowCard {
            Text("STATUS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Black)
            Text("ON TRACK", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = Success)
            Spacer(Modifier.height(DesignTokens.Spacing.Large))
            val progress = if (state.nodes.isEmpty()) 0f else {
                state.nodes.count { it.status == TaskStatus.DONE.name }.toFloat() / state.nodes.size
            }
            FlowProgress(progress = progress)
            Spacer(Modifier.height(DesignTokens.Spacing.Medium))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DetailStat("NEXT", state.nodes.find { it.id == state.nextTaskId }?.title ?: "Complete")
                DetailStat("DUE", "8:00 PM")
            }
        }
        Spacer(Modifier.height(DesignTokens.Spacing.Section))
        FlowPrimaryButton(text = "START FOCUS", onClick = onStartFocus, modifier = Modifier.fillMaxWidth(), icon = Icons.Filled.PlayArrow)
    }
}

@Composable
private fun OutcomeGraphTab(state: FlowUiState) {
    Column {
        FlowSectionHeader("CRITICAL PATH")
        Spacer(Modifier.height(DesignTokens.Spacing.Medium))
        state.nodes.forEachIndexed { index, task ->
            TechnicalGraphItem(
                title = task.title,
                isDone = task.status == TaskStatus.DONE.name,
                isNext = task.id == state.nextTaskId,
                isLast = index == state.nodes.lastIndex
            )
        }
    }
}

@Composable
private fun OutcomeTimelineTab(state: FlowUiState) {
    Column {
        FlowSectionHeader("PLANNED EXECUTION")
        Spacer(Modifier.height(DesignTokens.Spacing.Medium))
        state.nodes.forEach { task ->
            TimelineRow(task.title, task.deadlineLabel ?: "Pending")
        }
    }
}

@Composable
private fun OutcomeProofTab(state: FlowUiState) {
    Column {
        FlowSectionHeader("VERIFICATION EVIDENCE")
        Spacer(Modifier.height(DesignTokens.Spacing.Medium))
        if (state.evidence.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(140.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(DesignTokens.Shapes.Large)), contentAlignment = Alignment.Center) {
                Text("NO EVIDENCE ATTACHED", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Black)
            }
        } else {
            state.evidence.forEach { ev ->
                EvidenceItem(ev.type, ev.source, ev.timestamp)
            }
        }
    }
}

@Composable
private fun OutcomeActivityTab(state: FlowUiState) {
    Column {
        FlowSectionHeader("ACTIVITY TRAIL")
        Spacer(Modifier.height(DesignTokens.Spacing.Medium))
        if (state.activity.isEmpty()) {
            Text("No activity recorded.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            state.activity.forEach { act ->
                ActivityRow(act.title, act.type)
            }
        }
    }
}

@Composable
private fun DetailStat(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Black)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun TechnicalGraphItem(title: String, isDone: Boolean, isNext: Boolean, isLast: Boolean) {
    val color = if (isDone) Success else if (isNext) FlowAccent else MaterialTheme.colorScheme.outline
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(DesignTokens.Spacing.Large + 4.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f))
                    .border(2.dp, color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) Icon(Icons.Filled.Check, null, tint = Success, modifier = Modifier.size(14.dp))
            }
            if (!isLast) {
                Box(modifier = Modifier.width(2.dp).weight(1f).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)))
            }
        }
        Spacer(Modifier.width(DesignTokens.Spacing.Medium))
        Column(modifier = Modifier.padding(bottom = DesignTokens.Spacing.Huge)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = if (isNext) FontWeight.Black else FontWeight.Bold, color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
            if (isNext) Text("NEXT BEST ACTION", style = MaterialTheme.typography.labelSmall, color = FlowAccent, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun TimelineRow(title: String, time: String) {
    Row(modifier = Modifier.padding(vertical = DesignTokens.Spacing.Medium), verticalAlignment = Alignment.CenterVertically) {
        Text(time, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = FlowAccent, modifier = Modifier.width(DesignTokens.Spacing.Massive + 16.dp))
        Spacer(Modifier.width(DesignTokens.Spacing.Medium))
        Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EvidenceItem(type: String, source: String, timestamp: Long) {
    FlowCard(modifier = Modifier.padding(bottom = DesignTokens.Spacing.Medium)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Attachment, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(DesignTokens.Spacing.Medium))
            Column {
                Text(type, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(source, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ActivityRow(title: String, type: String) {
    Row(modifier = Modifier.padding(vertical = DesignTokens.Spacing.Small), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.outline, CircleShape))
        Spacer(Modifier.width(DesignTokens.Spacing.Medium))
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(type, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
