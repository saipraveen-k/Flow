package com.flowos.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.flowos.app.domain.model.LifeHub
import com.flowos.app.domain.model.Priority
import com.flowos.app.ui.ContextGraphViewModel
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.*

/**
 * LIFE CONTEXT: Premium Context Hub.
 * Sources: Professional, Personal, Learning, Fitness.
 */
@Composable
fun ContextScreen(viewModel: ContextGraphViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedHub by remember { mutableStateOf(LifeHub.PROFESSIONAL) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = DesignTokens.Spacing.Large),
    ) {
        Spacer(Modifier.height(DesignTokens.Spacing.Large))
        Text(
            "LIFE CONTEXT", 
            style = MaterialTheme.typography.headlineSmall, 
            fontWeight = FontWeight.Black, 
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(DesignTokens.Spacing.Tiny))
        Text(
            "Unified intelligence for work and life.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        
        Spacer(Modifier.height(DesignTokens.Spacing.Huge))

        // ---- CONTEXT SEGMENTS --------------------------------------------
        ScrollableTabRow(
            selectedTabIndex = selectedHub.ordinal,
            containerColor = Color.Transparent,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedHub.ordinal]),
                    color = FlowAccent
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
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selectedHub == hub) FontWeight.Black else FontWeight.Bold,
                            color = if (selectedHub == hub) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(DesignTokens.Spacing.Large))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            HubIntelligenceBrief(
                hub = selectedHub,
                icon = when(selectedHub) {
                    LifeHub.PROFESSIONAL -> Icons.Filled.Work
                    LifeHub.PERSONAL -> Icons.Filled.Person
                    LifeHub.LEARNING -> Icons.Filled.School
                    LifeHub.FITNESS -> Icons.Filled.FitnessCenter
                }
            )

            Spacer(Modifier.height(DesignTokens.Spacing.Huge))
            FlowSectionHeader("CAPACITY IMPACT")
            Spacer(Modifier.height(DesignTokens.Spacing.Medium))
            
            val impact = when(selectedHub) {
                LifeHub.PROFESSIONAL -> if (uiState.openTasks.isNotEmpty()) "${uiState.openTasks.size} Tasks · Priority Work" else "No active work"
                LifeHub.PERSONAL -> "Unified personal context signal"
                LifeHub.LEARNING -> "Academic context active"
                LifeHub.FITNESS -> if (uiState.fitnessMetrics?.workoutDurationMinutes != null) "${uiState.fitnessMetrics!!.workoutDurationMinutes}m workout today" else "Protected fitness windows"
            }
            
            CapacityImpactCard(impact)

            if (selectedHub == LifeHub.FITNESS) {
                Spacer(Modifier.height(DesignTokens.Spacing.Huge))
                FlowSectionHeader("HEALTH SIGNALS")
                Spacer(Modifier.height(DesignTokens.Spacing.Medium))
                
                val metrics = uiState.fitnessMetrics
                val steps = metrics?.steps?.toString() ?: "—"
                val calories = metrics?.activeCalories?.toInt()?.toString()?.plus(" kcal") ?: "—"
                
                HealthSignalCard("Steps Today", steps, Icons.Filled.DirectionsWalk)
                Spacer(Modifier.height(DesignTokens.Spacing.Medium))
                HealthSignalCard("Active Calories", calories, Icons.Filled.LocalFireDepartment)
            }

            if (uiState.openTasks.isNotEmpty() && selectedHub == LifeHub.PROFESSIONAL) {
                Spacer(Modifier.height(DesignTokens.Spacing.Huge))
                FlowSectionHeader("RELEVANT TASKS")
                Spacer(Modifier.height(DesignTokens.Spacing.Medium))
                uiState.openTasks.take(3).forEach { task ->
                    FlowTaskCard(
                        title = task.title,
                        deadlineLabel = task.deadlineLabel,
                        priority = Priority.from(task.priority),
                        checked = false,
                        onCheck = null,
                        modifier = Modifier.padding(bottom = DesignTokens.Spacing.Medium)
                    )
                }
            }
            
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun HubIntelligenceBrief(hub: LifeHub, icon: ImageVector) {
    FlowCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(DesignTokens.Shapes.Medium))
                    .background(FlowAccent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = FlowAccent, modifier = Modifier.size(DesignTokens.Spacing.Large + 4.dp))
            }
            Spacer(Modifier.width(DesignTokens.Spacing.Large))
            Column {
                Text(hub.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Text("CONTEXT SIGNAL ACTIVE", style = MaterialTheme.typography.labelSmall, color = FlowAccent, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun CapacityImpactCard(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Info.copy(alpha = 0.05f),
        shape = RoundedCornerShape(DesignTokens.Shapes.Medium),
        border = BorderStroke(1.dp, Info.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(DesignTokens.Spacing.Large), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Bolt, null, tint = Info, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(DesignTokens.Spacing.Medium))
            Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HealthSignalCard(label: String, value: String, icon: ImageVector) {
    FlowCard(backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Success, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(DesignTokens.Spacing.Medium))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Black)
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            }
        }
    }
}
