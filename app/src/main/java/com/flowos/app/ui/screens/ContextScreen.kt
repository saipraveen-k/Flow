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

/**
 * LIFE CONTEXT: Premium Multi-Layer Briefing.
 * Unified intelligence for Professional, Personal, Learning, and Fitness.
 */
@Composable
fun ContextScreen(viewModel: ContextGraphViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
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
            "Unified intelligence for your work and life.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
        )
        
        Spacer(Modifier.height(32.dp))

        // ---- FLAGSHIP SEGMENTED TABS: PRO, PERS, LEARN, FIT --------------
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
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selectedHub == hub) FontWeight.Black else FontWeight.Bold,
                            color = if (selectedHub == hub) Color.White else Color.Gray
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
            HubIntelligenceBriefing(
                hub = selectedHub,
                icon = when(selectedHub) {
                    LifeHub.PROFESSIONAL -> Icons.Filled.Work
                    LifeHub.PERSONAL -> Icons.Filled.Person
                    LifeHub.LEARNING -> Icons.Filled.School
                    LifeHub.FITNESS -> Icons.Filled.FitnessCenter
                }
            )

            Spacer(Modifier.height(32.dp))
            FlowSectionHeader("CONTEXTUAL KNOWLEDGE")
            Spacer(Modifier.height(16.dp))
            
            // Intelligence insights based on hub
            val insights = when(selectedHub) {
                LifeHub.PROFESSIONAL -> listOf("3 active work threads identified.", "Critical deadline today at 8 PM.")
                LifeHub.PERSONAL -> listOf("Family block detected at 6 PM.", "Utility bill due tomorrow.")
                LifeHub.LEARNING -> listOf("Exam sprint mode recommended.", "Algorithm session at 2 PM.")
                LifeHub.FITNESS -> listOf("Workout block at 7 PM (Protected).", "Recovery window identified.")
            }

            insights.forEach { insight ->
                FlagshipInsightCard(insight)
                Spacer(Modifier.height(12.dp))
            }
            
            if (state.openTasks.isNotEmpty()) {
                Spacer(Modifier.height(32.dp))
                FlowSectionHeader("PRIORITY STEPS")
                Spacer(Modifier.height(16.dp))
                state.openTasks.take(3).forEach { task ->
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
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun HubIntelligenceBriefing(hub: LifeHub, icon: ImageVector) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101010)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(28.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(FlowAccent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = FlowAccent, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(20.dp))
            Column {
                Text(hub.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White)
                Text("INTELLIGENCE LAYER ACTIVE", style = MaterialTheme.typography.labelSmall, color = FlowAccent, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
private fun FlagshipInsightCard(text: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Info, null, tint = Info, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(16.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
