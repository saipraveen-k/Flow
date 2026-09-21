package com.flowos.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.ui.HandoffState
import com.flowos.app.ui.OfficeKitViewModel
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.FlowAccent
import com.flowos.app.ui.theme.Success
import com.flowos.app.ui.theme.Warning

@Composable
fun OfficeKitScreen(
    viewModel: OfficeKitViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.onFileSelected(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070707))
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(32.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null, tint = Color.White) }
            Text(
                "OFFICE KIT",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp
            )
        }

        Spacer(Modifier.height(24.dp))

        // ---- CONNECTION STATUS -------------------------------------------
        ConnectionStatusCard(
            isConnected = state.isConnected, 
            deviceName = state.deviceName,
            onConnect = { viewModel.onConnectPC() }
        )

        state.currentOperation?.let { op ->
            Spacer(Modifier.height(16.dp))
            Text(op, style = MaterialTheme.typography.bodySmall, color = FlowAccent, fontWeight = FontWeight.Black)
        }

        Spacer(Modifier.height(32.dp))

        if (!state.isSupported) {
            OfficeKitUnsupportedState()
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            FlowSectionHeader("SYSTEM CAPABILITIES")
            Spacer(Modifier.height(16.dp))

            CapabilityTile(
                icon = Icons.Filled.SendToMobile,
                title = "FILE TRANSFER",
                description = "Phone ↔ PC rapid sharing",
                onClick = { fileLauncher.launch("*/*") }
            )
            CapabilityTile(
                icon = Icons.Filled.LaptopMac,
                title = "TASK HANDOFF",
                description = "Continue work on PC",
                onClick = { /* Open task handoff */ }
            )
            CapabilityTile(
                icon = Icons.Filled.OpenInBrowser,
                title = "OPEN ON PC",
                description = "Directly open links/files",
                onClick = { /* Open on PC */ }
            )

            Spacer(Modifier.height(32.dp))
            FlowSectionHeader("RECENT HANDOFFS")
            Spacer(Modifier.height(16.dp))

            if (state.recentHandoffs.isEmpty()) {
                Text("No recent activity.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            } else {
                state.recentHandoffs.forEach { handoff ->
                    HandoffItem(handoff)
                }
            }
            
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun ConnectionStatusCard(isConnected: Boolean, deviceName: String?, onConnect: () -> Unit) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101010)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (isConnected) Success else Warning)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = if (isConnected) "PC CONNECTED" else "DISCONNECTED",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = if (isConnected) Success else Warning,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (isConnected) deviceName ?: "Unknown Device" else "Connect your PC to continue.",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(Modifier.weight(1f))
            if (!isConnected) {
                TextButton(onClick = onConnect) {
                    Text("CONNECT", color = FlowAccent, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun CapabilityTile(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        color = Color(0xFF161616),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
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
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color.White)
                Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Filled.ChevronRight, null, tint = Color.DarkGray)
        }
    }
}

@Composable
private fun HandoffItem(handoff: HandoffState) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Description, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(handoff.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
            Text(handoff.status, style = MaterialTheme.typography.labelSmall, color = FlowAccent)
        }
        Text(handoff.timestamp, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
private fun OfficeKitUnsupportedState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Filled.CloudOff, null, modifier = Modifier.size(64.dp), tint = Color.DarkGray)
        Spacer(Modifier.height(24.dp))
        Text("OFFICE KIT NOT AVAILABLE", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White)
        Spacer(Modifier.height(8.dp))
        Text(
            "This device doesn't support native Office Kit. Use the system share sheet for cross-device workflows.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(40.dp))
        FlowPrimaryButton(text = "USE ANDROID SHARE", onClick = { /* Fallback */ })
    }
}
