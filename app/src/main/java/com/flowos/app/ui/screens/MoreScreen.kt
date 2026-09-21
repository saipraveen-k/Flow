package com.flowos.app.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowos.app.ui.SettingsViewModel
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.FlowAccent

/**
 * MORE: Flagship System Settings and Secondary Tools.
 * Organized into logical sections: Execution, Memory, Device, System.
 */
@Composable
fun MoreScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onNavigateToStrategies: () -> Unit,
    onNavigateToMemory: () -> Unit,
    onNavigateToFlowSpace: () -> Unit,
    onNavigateToOfficeKit: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToActivity: () -> Unit,
    onNavigateToFocus: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070707))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(32.dp))
        Text(
            "SYSTEM", 
            style = MaterialTheme.typography.headlineMedium, 
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(32.dp))

        // ---- GROUP: EXECUTION --------------------------------------------
        FlowSectionHeader("EXECUTION")
        Spacer(Modifier.height(12.dp))
        SystemMenuItem(Icons.Filled.Timer, "FOCUS", "Enter deep execution mode", onNavigateToFocus)
        SystemMenuItem(Icons.Filled.RocketLaunch, "STRATEGIES", "Apply productivity patterns", onNavigateToStrategies)
        
        Spacer(Modifier.height(32.dp))

        // ---- GROUP: MEMORY -----------------------------------------------
        FlowSectionHeader("MEMORY")
        Spacer(Modifier.height(12.dp))
        SystemMenuItem(Icons.Filled.Memory, "FLOW MEMORY", "Project context & decisions", onNavigateToMemory)
        SystemMenuItem(Icons.Filled.Folder, "FLOW SPACE", "Files, shots & documents", onNavigateToFlowSpace)
        
        Spacer(Modifier.height(32.dp))
        
        // ---- GROUP: DEVICE -----------------------------------------------
        FlowSectionHeader("DEVICE")
        Spacer(Modifier.height(12.dp))
        SystemMenuItem(Icons.Filled.Devices, "OFFICE KIT", "Phone ↔ PC rapid sharing", onNavigateToOfficeKit)
        
        Spacer(Modifier.height(32.dp))
        
        // ---- GROUP: SYSTEM -----------------------------------------------
        FlowSectionHeader("SYSTEM")
        Spacer(Modifier.height(12.dp))
        SystemMenuItem(Icons.Filled.Settings, "SETTINGS", "App appearance & preferences", onNavigateToSettings)
        SystemMenuItem(Icons.Filled.Lock, "PRIVACY CENTER", "On-device data management", onNavigateToPrivacy)
        SystemMenuItem(Icons.Filled.History, "ACTIVITY LOG", "Full audit trail of actions", onNavigateToActivity)

        Spacer(Modifier.height(48.dp))
        
        // ---- MAINTENANCE -------------------------------------------------
        OutlinedButton(
            onClick = { viewModel.clearAllData { onBack() } },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
        ) {
            Icon(Icons.Filled.DeleteSweep, null)
            Spacer(Modifier.width(12.dp))
            Text("RESET SYSTEM DATA", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }

        Spacer(Modifier.height(48.dp))
        
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("FLOWOS V3.0", style = MaterialTheme.typography.labelSmall, color = Color.DarkGray, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Text("FLAGSHIP EDITION", style = MaterialTheme.typography.labelSmall, color = Color.DarkGray, fontWeight = FontWeight.Medium)
        }
        
        Spacer(Modifier.height(80.dp)) // Extra padding for FAB/BottomBar
    }
}

@Composable
private fun SystemMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        color = Color(0xFF101010),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FlowAccent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = FlowAccent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(20.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color.White)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(Icons.Filled.ChevronRight, null, tint = Color.DarkGray)
        }
    }
}
