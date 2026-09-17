package com.flowos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowos.app.domain.model.Strategy
import com.flowos.app.ui.StrategyViewModel
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.FlowAccent
import com.flowos.app.workflow.StrategyEngine

@Composable
fun StrategyScreen(
    viewModel: StrategyViewModel,
    onBack: () -> Unit,
    onStrategyApplied: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070707))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null, tint = Color.White) }
            Text(
                "FLOW STRATEGIES", 
                style = MaterialTheme.typography.headlineSmall, 
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Predefined productivity patterns.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            modifier = Modifier.padding(start = 12.dp)
        )
        
        Spacer(Modifier.height(28.dp))

        FlowSectionHeader("SYSTEM STRATEGIES")
        Spacer(Modifier.height(16.dp))
        
        StrategyEngine.PREDEFINED_STRATEGIES.forEach { strategy ->
            FlagshipStrategyCard(strategy) {
                viewModel.useStrategy(strategy, onStrategyApplied)
            }
            Spacer(Modifier.height(16.dp))
        }

        Spacer(Modifier.height(32.dp))
        FlowPrimaryButton(
            text = "CREATE CUSTOM STRATEGY",
            onClick = { /* Builder reserved */ },
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Filled.Add,
            enabled = false
        )
        
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun FlagshipStrategyCard(strategy: Strategy, onUse: () -> Unit) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101010)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(28.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FlowAccent.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when(strategy.id) {
                            "strat_deep_work" -> Icons.Filled.Psychology
                            "strat_exam_sprint" -> Icons.Filled.School
                            else -> Icons.Filled.RocketLaunch
                        },
                        contentDescription = null,
                        tint = FlowAccent
                    )
                }
                Spacer(Modifier.width(20.dp))
                Text(strategy.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White)
            }
            Spacer(Modifier.height(16.dp))
            Text(strategy.description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(Modifier.height(24.dp))
            Text("TIME STRUCTURE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = FlowAccent, letterSpacing = 2.sp)
            Spacer(Modifier.height(12.dp))
            
            strategy.steps.forEach { step ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Icon(Icons.Filled.Bolt, null, modifier = Modifier.size(14.dp), tint = FlowAccent)
                    Spacer(Modifier.width(12.dp))
                    Text(step.title, style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text("${step.estimatedDurationMinutes}m", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Black)
                }
            }
            
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = onUse,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f))
            ) {
                Text("USE STRATEGY", fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp)
            }
        }
    }
}
