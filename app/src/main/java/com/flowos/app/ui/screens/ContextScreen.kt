package com.flowos.app.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.domain.model.LifeHub
import com.flowos.app.domain.model.Priority
import com.flowos.app.ui.ContextGraphUiState
import com.flowos.app.ui.ContextGraphViewModel
import com.flowos.app.ui.components.*

@Composable
fun ContextScreen(viewModel: ContextGraphViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedHub by remember { mutableStateOf(LifeHub.PROFESSIONAL) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Text("LIFE CONTEXT", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(4.dp))
        Text(
            "Unified source signals for your plan.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        
        Spacer(Modifier.height(24.dp))

        // ---- HUB TABS: PRO, PERS, LEARN, FIT -----------------------------
        ScrollableTabRow(
            selectedTabIndex = selectedHub.ordinal,
            containerColor = Color.Transparent,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedHub.ordinal]),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            edgePadding = 0.dp
        ) {
            LifeHub.entries.forEach { hub ->
                Tab(
                    selected = selectedHub == hub,
                    onClick = { selectedHub = hub },
                    text = {
                        Text(
                            text = hub.name,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selectedHub == hub) FontWeight.Black else FontWeight.Bold
                        )
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            when (selectedHub) {
                LifeHub.PROFESSIONAL -> HubContent(
                    title = "Professional Context",
                    icon = Icons.Filled.Work,
                    items = listOf("Hackathon Demo", "Architecture Review", "Team Sync"),
                    state = state
                )
                LifeHub.PERSONAL -> HubContent(
                    title = "Personal Context",
                    icon = Icons.Filled.Person,
                    items = listOf("Family Dinner", "Grocery Run", "Utility Bills"),
                    state = state
                )
                LifeHub.LEARNING -> HubContent(
                    title = "Learning Context",
                    icon = Icons.Filled.School,
                    items = listOf("Advanced Kotlin", "Material 3 Design", "DSA Practice"),
                    state = state
                )
                LifeHub.FITNESS -> HubContent(
                    title = "Fitness Context",
                    icon = Icons.Filled.FitnessCenter,
                    items = listOf("Morning Run", "Yoga Session", "Strength Training"),
                    state = state
                )
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun HubContent(
    title: String,
    icon: ImageVector,
    items: List<String>,
    state: ContextGraphUiState
) {
    Column {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text("Source signal active", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        FlowSectionHeader("ACTIVE OUTCOMES")
        Spacer(Modifier.height(12.dp))
        
        items.forEach { item ->
            PulseCard(modifier = Modifier.padding(bottom = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                    Spacer(Modifier.width(12.dp))
                    Text(item, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        
        if (state.openTasks.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            FlowSectionHeader("RELATED TASKS")
            Spacer(Modifier.height(12.dp))
            state.openTasks.take(3).forEach { task ->
                FlowTaskCard(
                    title = task.title,
                    deadlineLabel = task.deadlineLabel,
                    priority = Priority.from(task.priority),
                    checked = false,
                    onCheck = null,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
        }
    }
}
