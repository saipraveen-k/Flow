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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.domain.model.Priority
import com.flowos.app.domain.model.TaskStatus
import com.flowos.app.ui.FlowUiState
import com.flowos.app.ui.FlowViewModel
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.FlowAccent
import com.flowos.app.ui.theme.Success

/**
 * OUTCOME DETAIL: Flagship Command Center.
 * Multi-surface view of progress, work graph, and intelligence context.
 */
@Composable
fun OutcomeDetailScreen(
    viewModel: FlowViewModel,
    onStartFocus: () -> Unit,
    onCapture: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070707))
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null, tint = Color.White) }
            Text(
                text = state.projectName?.uppercase() ?: "OUTCOME",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp
            )
        }
        
        Spacer(Modifier.height(16.dp))
        
        // ---- FLAGSHIP SEGMENTED NAVIGATION -------------------------------
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = FlowAccent
                )
            }
        ) {
            FlagshipTab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = "OVERVIEW")
            FlagshipTab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = "WORK GRAPH")
            FlagshipTab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = "TIMELINE")
        }

        Spacer(Modifier.height(24.dp))

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
                }
            }
            Spacer(Modifier.height(40.dp))
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
                style = MaterialTheme.typography.labelLarge, 
                fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
                color = if (selected) Color.White else Color.Gray
            )
        }
    )
}

@Composable
private fun OutcomeOverviewTab(state: FlowUiState, onStartFocus: () -> Unit) {
    Column {
        // ---- STATUS CARD: Hero progress indicator -----------------------
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101010)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(28.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(FlowAccent, CircleShape))
                    Spacer(Modifier.width(12.dp))
                    Text("CURRENT EXECUTION", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.Gray)
                }
                Spacer(Modifier.height(8.dp))
                Text("ON TRACK", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = Success)
                Spacer(Modifier.height(24.dp))
                
                val progress = if (state.nodes.isEmpty()) 0f else {
                    state.nodes.count { it.status == TaskStatus.DONE.name }.toFloat() / state.nodes.size
                }
                FlowProgress(progress = progress)
                
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    BriefStat("DEADLINE", "8:00 PM")
                    BriefStat("TASKS", "${state.nodes.size} steps")
                    BriefStat("RISKS", "None")
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        FlowSectionHeader("NEXT ACTIONABLE STEP")
        Spacer(Modifier.height(12.dp))
        
        state.nodes.find { it.id == state.nextTaskId }?.let { task ->
            FlowTaskCard(
                title = task.title,
                deadlineLabel = task.deadlineLabel,
                priority = Priority.from(task.priority),
                checked = false,
                onCheck = null
            )
        }

        Spacer(Modifier.height(32.dp))
        FlowPrimaryButton(
            text = "CONTINUE FLOW",
            onClick = onStartFocus,
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Filled.PlayArrow
        )
    }
}

@Composable
private fun OutcomeGraphTab(state: FlowUiState) {
    Column {
        FlowSectionHeader("VISUAL WORK GRAPH")
        Spacer(Modifier.height(16.dp))
        
        state.nodes.forEachIndexed { index, task ->
            GraphPathItem(
                title = task.title,
                isCompleted = task.status == TaskStatus.DONE.name,
                isNext = task.id == state.nextTaskId,
                isLast = index == state.nodes.lastIndex
            )
        }
    }
}

@Composable
private fun OutcomeTimelineTab(state: FlowUiState) {
    Column {
        FlowSectionHeader("EXECUTION TIMELINE")
        Spacer(Modifier.height(16.dp))
        
        state.nodes.forEach { task ->
            Row(
                modifier = Modifier.padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.deadlineLabel ?: "TBD", 
                    style = MaterialTheme.typography.labelMedium, 
                    fontWeight = FontWeight.Black, 
                    color = Color.Gray,
                    modifier = Modifier.width(64.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(task.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("30 min estimated", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun BriefStat(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Black)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black, color = Color.White)
    }
}

@Composable
private fun GraphPathItem(title: String, isCompleted: Boolean, isNext: Boolean, isLast: Boolean) {
    val nodeColor = if (isCompleted) Success else if (isNext) FlowAccent else Color.DarkGray
    
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(nodeColor.copy(alpha = 0.1f))
                    .border(2.dp, nodeColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) Icon(Icons.Filled.Check, null, tint = Success, modifier = Modifier.size(16.dp))
                else if (isNext) Box(modifier = Modifier.size(8.dp).background(FlowAccent, CircleShape))
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(Color.Gray.copy(alpha = 0.2f))
                )
            }
        }
        Spacer(Modifier.width(20.dp))
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isNext) FontWeight.Black else FontWeight.Bold,
                color = if (isCompleted) Color.Gray else Color.White
            )
            if (isNext) {
                Text("CURRENT FOCUS", style = MaterialTheme.typography.labelSmall, color = FlowAccent, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }
    }
}
