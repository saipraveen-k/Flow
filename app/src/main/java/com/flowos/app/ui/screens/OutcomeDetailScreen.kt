package com.flowos.app.ui.screens

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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.domain.model.Priority
import com.flowos.app.domain.model.TaskStatus
import com.flowos.app.ui.FlowUiState
import com.flowos.app.ui.FlowViewModel
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.Success

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
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) }
            Text(state.projectName ?: "Outcome", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        }
        
        Spacer(Modifier.height(16.dp))
        
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("OVERVIEW") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("GRAPH") })
        }

        Spacer(Modifier.height(20.dp))

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            if (selectedTab == 0) {
                OutcomeOverviewContent(state, onStartFocus)
            } else {
                OutcomeGraphContent(state)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun OutcomeOverviewContent(state: FlowUiState, onStartFocus: () -> Unit) {
    Column {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(24.dp)) {
                FlowSectionHeader("STATUS")
                Text("IN PROGRESS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(16.dp))
                val progress = if (state.nodes.isEmpty()) 0f else {
                    state.nodes.count { it.status == TaskStatus.DONE.name }.toFloat() / state.nodes.size
                }
                FlowProgress(progress = progress)
                Spacer(Modifier.height(16.dp))
                Text("Due Today · 8:00 PM", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(28.dp))
        FlowSectionHeader("TASKS")
        Spacer(Modifier.height(12.dp))
        state.nodes.forEach { task ->
            FlowTaskCard(
                title = task.title,
                deadlineLabel = task.deadlineLabel,
                priority = Priority.from(task.priority),
                checked = task.status == TaskStatus.DONE.name,
                onCheck = {}, // Read only here
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        Spacer(Modifier.height(24.dp))
        FlowPrimaryButton(text = "CONTINUE FOCUS", onClick = onStartFocus, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun OutcomeGraphContent(state: FlowUiState) {
    Column {
        FlowSectionHeader("CRITICAL PATH")
        Spacer(Modifier.height(12.dp))
        state.nodes.forEachIndexed { index, task ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val color = if (task.status == TaskStatus.DONE.name) Success else MaterialTheme.colorScheme.primary
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
                    if (index != state.nodes.lastIndex) {
                        Box(modifier = Modifier.width(2.dp).height(40.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Text(task.title, style = MaterialTheme.typography.bodyLarge, fontWeight = if (task.id == state.nextTaskId) FontWeight.Black else FontWeight.Bold, color = if (task.status == TaskStatus.DONE.name) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
