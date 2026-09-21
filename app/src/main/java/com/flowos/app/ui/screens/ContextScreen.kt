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
import com.flowos.app.ui.theme.FlowAccent
import com.flowos.app.ui.theme.Info
import com.flowos.app.ui.theme.Success

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
            .background(Color(0xFF070707))
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            "LIFE CONTEXT", 
            style = MaterialTheme.typography.headlineSmall, 
            fontWeight = FontWeight.Black, 
            color = Color.White,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Unified intelligence for work and life.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
        )
        
        Spacer(Modifier.height(32.dp))

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
                            color = if (selectedHub == hub) Color.White else Color.Gray,
                            letterSpacing = 1.sp
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

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

            Spacer(Modifier.height(32.dp))
            FlowSectionHeader("CAPACITY IMPACT")
            Spacer(Modifier.height(12.dp))
            
            val impact = when(selectedHub) {
                LifeHub.PROFESSIONAL -> if (uiState.openTasks.isNotEmpty()) "${uiState.openTasks.size} Tasks · Priority Work" else "No active work"
                LifeHub.PERSONAL -> "Unified personal context signal"
                LifeHub.LEARNING -> "Academic context active"
                LifeHub.FITNESS -> if (uiState.fitnessMetrics?.workoutDurationMinutes != null) "${uiState.fitnessMetrics!!.workoutDurationMinutes}m workout today" else "Protected fitness windows"
            }
            
            CapacityImpactCard(impact)

            if (selectedHub == LifeHub.FITNESS) {
                Spacer(Modifier.height(32.dp))
                FlowSectionHeader("HEALTH SIGNALS")
                Spacer(Modifier.height(12.dp))
                
                val steps = uiState.fitnessMetrics?.steps?.toString() ?: "—"
                val calories = uiState.fitnessMetrics?.activeCalories?.toInt()?.toString()?.plus(" kcal") ?: "—"
                
                HealthSignalCard("Steps Today", steps, Icons.Filled.DirectionsWalk)
                Spacer(Modifier.height(12.dp))
                HealthSignalCard("Active Calories", calories, Icons.Filled.LocalFireDepartment)
            }

            if (uiState.openTasks.isNotEmpty() && selectedHub == LifeHub.PROFESSIONAL) {
                Spacer(Modifier.height(32.dp))
                FlowSectionHeader("RELEVANT TASKS")
                Spacer(Modifier.height(16.dp))
                uiState.openTasks.take(3).forEach { task ->
                    FlowTaskCard(
                        title = task.title,
                        deadlineLabel = task.deadlineLabel,
                        priority = Priority.from(task.priority),
                        checked = false,
                        onCheck = null,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun HubIntelligenceBrief(hub: LifeHub, icon: ImageVector) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101010)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(28.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(FlowAccent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = FlowAccent, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(20.dp))
            Column {
                Text(hub.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White)
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
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Info.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Bolt, null, tint = Info, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(16.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun HealthSignalCard(label: String, value: String, icon: ImageVector) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Success, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Black)
                Text(value, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Black)
            }
        }
    }
}
