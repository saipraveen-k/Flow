package com.flowos.app.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowos.app.ui.SettingsViewModel
import com.flowos.app.ui.components.*

@Composable
fun MoreScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onNavigateToStrategies: () -> Unit,
    onNavigateToMemory: () -> Unit,
    onNavigateToActivity: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Text("MORE", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(24.dp))

        // ---- NAVIGATION GROUP --------------------------------------------
        FlowSectionHeader("SYSTEM")
        Spacer(Modifier.height(12.dp))
        
        MoreMenuItem(Icons.Filled.RocketLaunch, "STRATEGIES", "Predefined work patterns", onNavigateToStrategies)
        MoreMenuItem(Icons.Filled.Memory, "FLOW MEMORY", "Context, decisions & questions", onNavigateToMemory)
        MoreMenuItem(Icons.Filled.History, "ACTIVITY LOG", "Audit trail of executions", onNavigateToActivity)
        
        Spacer(Modifier.height(32.dp))
        
        // ---- SETTINGS GROUP ----------------------------------------------
        FlowSectionHeader("CONFIGURATION")
        Spacer(Modifier.height(12.dp))
        
        PulseCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.SmartToy, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("AI PROCESSING MODE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("On-device deterministic heuristics", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        
        // ---- DANGER ZONE -------------------------------------------------
        FlowSectionHeader("MAINTENANCE")
        Spacer(Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = { viewModel.clearAllData { onBack() } },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Filled.DeleteSweep, null)
            Spacer(Modifier.width(12.dp))
            Text("RESET ALL DATA", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun MoreMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
