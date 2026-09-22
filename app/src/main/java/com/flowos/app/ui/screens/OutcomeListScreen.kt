package com.flowos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.ui.ActivityViewModel
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.*

/**
 * OUTCOMES: Flagship Command Center.
 */
@Composable
fun OutcomeListScreen(
    viewModel: ActivityViewModel,
    onOutcomeClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = DesignTokens.Spacing.Large),
    ) {
        Spacer(Modifier.height(DesignTokens.Spacing.Large))
        Text(
            "OUTCOMES", 
            style = MaterialTheme.typography.headlineSmall, 
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(DesignTokens.Spacing.Tiny))
        Text(
            "Manage your measurable results.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        
        Spacer(Modifier.height(DesignTokens.Spacing.Huge))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            FlowSectionHeader("ACTIVE")
            Spacer(Modifier.height(DesignTokens.Spacing.Medium))
            
            if (uiState.activeOutcomes.isEmpty()) {
                FlowEmptyState(title = "No active outcomes", description = "Capture a goal to begin.")
            } else {
                uiState.activeOutcomes.forEach { outcome ->
                    FlagshipOutcomeCard(
                        title = outcome.title,
                        progress = outcome.progressPercent,
                        status = outcome.status,
                        onClick = { onOutcomeClick(outcome.id) }
                    )
                    Spacer(Modifier.height(DesignTokens.Spacing.Medium))
                }
            }

            Spacer(Modifier.height(DesignTokens.Spacing.Huge))
            FlowSectionHeader("COMPLETED")
            Spacer(Modifier.height(DesignTokens.Spacing.Medium))
            
            if (uiState.completedOutcomes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(DesignTokens.Shapes.Large)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("NO COMPLETED OUTCOMES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Black)
                }
            } else {
                uiState.completedOutcomes.forEach { outcome ->
                    FlagshipOutcomeCard(
                        title = outcome.title,
                        progress = outcome.progressPercent,
                        status = outcome.status,
                        onClick = { onOutcomeClick(outcome.id) }
                    )
                    Spacer(Modifier.height(DesignTokens.Spacing.Medium))
                }
            }
            
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun FlagshipOutcomeCard(
    title: String,
    progress: Int,
    status: String,
    onClick: () -> Unit
) {
    FlowCard(onClick = onClick) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title.uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            StatusPill(status)
        }
        Spacer(Modifier.height(DesignTokens.Spacing.Large))
        FlowProgress(progress = progress / 100f)
        Spacer(Modifier.height(DesignTokens.Spacing.Medium))
        Text("${progress}% COMPLETE", style = MaterialTheme.typography.labelSmall, color = FlowAccent, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun StatusPill(status: String) {
    Surface(
        color = Success.copy(alpha = 0.1f),
        shape = RoundedCornerShape(DesignTokens.Shapes.Small)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Success,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )
    }
}
