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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.ui.HandoffState
import com.flowos.app.ui.OfficeKitViewModel
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.*

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
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = DesignTokens.Spacing.Large),
    ) {
        Spacer(Modifier.height(DesignTokens.Spacing.Large))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) }
            Text(
                "OFFICE KIT",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        Spacer(Modifier.height(DesignTokens.Spacing.Huge))

        // ---- CONNECTION STATUS ----
        ConnectionStatusCard(
            isConnected = state.isConnected, 
            deviceName = state.deviceName,
            onConnect = { viewModel.onConnectPC() }
        )

        state.currentOperation?.let { op ->
            Spacer(Modifier.height(DesignTokens.Spacing.Medium))
            Text(op, style = MaterialTheme.typography.labelSmall, color = FlowAccent, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp))
        }

        Spacer(Modifier.height(DesignTokens.Spacing.Huge))

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
            Spacer(Modifier.height(DesignTokens.Spacing.Medium))

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

            Spacer(Modifier.height(DesignTokens.Spacing.Huge))
            FlowSectionHeader("RECENT HANDOFFS")
            Spacer(Modifier.height(DesignTokens.Spacing.Medium))

            if (state.recentHandoffs.isEmpty()) {
                Text("No recent activity.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 8.dp))
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
    FlowCard(backgroundColor = if (isConnected) Success.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (isConnected) Success else Warning)
            )
            Spacer(Modifier.width(DesignTokens.Spacing.Large))
            Column {
                Text(
                    text = if (isConnected) "SYSTEM LINKED" else "DISCONNECTED",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = if (isConnected) Success else Warning,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (isConnected) deviceName ?: "Unknown Device" else "Pair with your PC to continue.",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
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
        modifier = Modifier.fillMaxWidth().padding(bottom = DesignTokens.Spacing.Medium),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(DesignTokens.Shapes.Large),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(DesignTokens.Spacing.Large), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(DesignTokens.Shapes.Small))
                    .background(FlowAccent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = FlowAccent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(DesignTokens.Spacing.Large))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun HandoffItem(handoff: HandoffState) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = DesignTokens.Spacing.Medium), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Description, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(DesignTokens.Spacing.Medium))
        Column(Modifier.weight(1f)) {
            Text(handoff.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(handoff.status, style = MaterialTheme.typography.labelSmall, color = FlowAccent)
        }
        Text(handoff.timestamp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun OfficeKitUnsupportedState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Filled.CloudOff, null, modifier = Modifier.size(DesignTokens.Spacing.Hero + 24.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.height(DesignTokens.Spacing.Large))
        Text("OFFICE KIT UNAVAILABLE", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(DesignTokens.Spacing.Small))
        Text(
            "This device doesn't support native Office Kit. Use the system share sheet for cross-device workflows.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = DesignTokens.Spacing.Large)
        )
        Spacer(Modifier.height(DesignTokens.Spacing.Huge))
        FlowPrimaryButton(text = "USE ANDROID SHARE", onClick = { /* Fallback */ })
    }
}
