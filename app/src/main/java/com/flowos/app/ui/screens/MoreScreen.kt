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
 * MORE: Flagship System Settings.
 * Minimal, technical, and clean.
 */
@Composable
fun MoreScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onNavigateToStrategies: () -> Unit,
    onNavigateToMemory: () -> Unit,
    onNavigateToActivity: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToOfficeKit: () -> Unit
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

        // ---- GROUP: INTELLIGENCE -----------------------------------------
        FlowSectionHeader("INTELLIGENCE LAYER")
        Spacer(Modifier.height(12.dp))
        
        SystemMenuItem(Icons.Filled.RocketLaunch, "STRATEGIES", "Predefined execution patterns", onNavigateToStrategies)
        SystemMenuItem(Icons.Filled.Memory, "FLOW MEMORY", "Project context & decisions", onNavigateToMemory)
        SystemMenuItem(Icons.Filled.History, "ACTIVITY LOG", "Full audit trail of actions", onNavigateToActivity)
        
        Spacer(Modifier.height(32.dp))
        
        // ---- GROUP: DEVICE -----------------------------------------------
        FlowSectionHeader("DEVICE & PRIVACY")
        Spacer(Modifier.height(12.dp))
        
        SystemMenuItem(Icons.Filled.Devices, "OFFICE KIT", "Cross-device flow sharing", onNavigateToOfficeKit)
        SystemMenuItem(Icons.Filled.Lock, "PRIVACY CENTER", "Local-first data management", onNavigateToPrivacy)

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
        
        Spacer(Modifier.height(60.dp))
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
