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
import com.flowos.app.ui.theme.FlowAccent
import com.flowos.app.ui.theme.Success
import com.flowos.app.ui.theme.Warning

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
            .background(Color(0xFF070707))
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            "OUTCOMES", 
            style = MaterialTheme.typography.headlineSmall, 
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Manage your measurable results.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
        )
        
        Spacer(Modifier.height(28.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            FlowSectionHeader("ACTIVE")
            Spacer(Modifier.height(12.dp))
            
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
                    Spacer(Modifier.height(12.dp))
                }
            }

            Spacer(Modifier.height(32.dp))
            FlowSectionHeader("COMPLETED")
            Spacer(Modifier.height(12.dp))
            
            if (uiState.completedOutcomes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("NO COMPLETED OUTCOMES", style = MaterialTheme.typography.labelSmall, color = Color.DarkGray, fontWeight = FontWeight.Black)
                }
            } else {
                uiState.completedOutcomes.forEach { outcome ->
                    FlagshipOutcomeCard(
                        title = outcome.title,
                        progress = outcome.progressPercent,
                        status = outcome.status,
                        onClick = { onOutcomeClick(outcome.id) }
                    )
                    Spacer(Modifier.height(12.dp))
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
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101010)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title.uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color.White)
                StatusPill(status)
            }
            Spacer(Modifier.height(20.dp))
            FlowProgress(progress = progress / 100f)
            Spacer(Modifier.height(12.dp))
            Text("${progress}% COMPLETE", style = MaterialTheme.typography.labelSmall, color = FlowAccent, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun StatusPill(status: String) {
    Surface(
        color = Success.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
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
