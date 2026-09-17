package com.flowos.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowos.app.domain.model.Strategy
import com.flowos.app.ui.StrategyViewModel
import com.flowos.app.ui.components.*
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) }
            Text("STRATEGIES", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Predefined productivity patterns.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp)
        )
        
        Spacer(Modifier.height(24.dp))

        FlowSectionHeader("PREDEFINED")
        Spacer(Modifier.height(12.dp))
        
        StrategyEngine.PREDEFINED_STRATEGIES.forEach { strategy ->
            StrategyCard(strategy) {
                viewModel.useStrategy(strategy, onStrategyApplied)
            }
            Spacer(Modifier.height(16.dp))
        }

        Spacer(Modifier.height(24.dp))
        FlowPrimaryButton(
            text = "CREATE CUSTOM STRATEGY",
            onClick = { /* Custom Strategy Builder is reserved for future updates */ },
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Filled.Add,
            enabled = false
        )
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun StrategyCard(strategy: Strategy, onUse: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when(strategy.id) {
                            "strat_deep_work" -> Icons.Filled.Psychology
                            "strat_exam_sprint" -> Icons.Filled.School
                            else -> Icons.Filled.RocketLaunch
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.width(16.dp))
                Text(strategy.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(12.dp))
            Text(strategy.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            Text("CREATES:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Spacer(Modifier.height(8.dp))
            strategy.steps.forEach { step ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                    Icon(Icons.Filled.Check, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(step.title, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.width(8.dp))
                    Text("${step.estimatedDurationMinutes}m", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onUse,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("USE STRATEGY", fontWeight = FontWeight.Bold)
            }
        }
    }
}
