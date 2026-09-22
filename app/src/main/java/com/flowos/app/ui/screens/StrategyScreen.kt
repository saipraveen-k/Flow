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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowos.app.domain.model.Strategy
import com.flowos.app.ui.StrategyViewModel
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.*
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
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DesignTokens.Spacing.Large),
    ) {
        Spacer(Modifier.height(DesignTokens.Spacing.Large))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) }
            Text(
                "STRATEGIES", 
                style = MaterialTheme.typography.headlineSmall, 
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
        Spacer(Modifier.height(DesignTokens.Spacing.Tiny))
        Text(
            "Predefined productivity patterns.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        
        Spacer(Modifier.height(DesignTokens.Spacing.Huge))

        FlowSectionHeader("SYSTEM STRATEGIES")
        Spacer(Modifier.height(DesignTokens.Spacing.Medium))
        
        StrategyEngine.PREDEFINED_STRATEGIES.forEach { strategy ->
            FlagshipStrategyCard(strategy) {
                viewModel.useStrategy(strategy, onStrategyApplied)
            }
            Spacer(Modifier.height(DesignTokens.Spacing.Large))
        }

        Spacer(Modifier.height(DesignTokens.Spacing.Huge))
        FlowPrimaryButton(
            text = "CREATE CUSTOM",
            onClick = { },
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Filled.Add,
            enabled = false
        )
        
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun FlagshipStrategyCard(strategy: Strategy, onUse: () -> Unit) {
    FlowCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(DesignTokens.Shapes.Medium))
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
            Spacer(Modifier.width(DesignTokens.Spacing.Large))
            Text(strategy.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(DesignTokens.Spacing.Medium))
        Text(strategy.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(DesignTokens.Spacing.Large))
        Text("TIME STRUCTURE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = FlowAccent, letterSpacing = 2.sp)
        Spacer(Modifier.height(DesignTokens.Spacing.Medium))
        
        strategy.steps.forEach { step ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = DesignTokens.Spacing.Tiny)) {
                Icon(Icons.Filled.Bolt, null, modifier = Modifier.size(14.dp), tint = FlowAccent)
                Spacer(Modifier.width(DesignTokens.Spacing.Medium))
                Text(step.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("${step.estimatedDurationMinutes}m", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Black)
            }
        }
        
        Spacer(Modifier.height(DesignTokens.Spacing.Large))
        FlowPrimaryButton(
            text = "USE STRATEGY",
            onClick = onUse,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }
}
