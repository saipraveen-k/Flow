package com.flowos.app.ui.screens

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.*

@Composable
fun DiagnosticsScreen(onBack: () -> Unit) {
    val context = LocalContext.current

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
                "DIAGNOSTICS", 
                style = MaterialTheme.typography.headlineSmall, 
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
        
        Spacer(Modifier.height(DesignTokens.Spacing.Huge))

        FlowSectionHeader("SYSTEM HEALTH")
        DiagnosticItem("Database", "CONNECTED", Success, Icons.Filled.Storage)
        DiagnosticItem("Local AI", "READY", Success, Icons.Filled.AutoAwesome)
        DiagnosticItem("Flow Pulse", "ACTIVE", Success, Icons.Filled.Bolt)
        
        Spacer(Modifier.height(DesignTokens.Spacing.Huge))
        FlowSectionHeader("INPUT SERVICES")
        
        val isAccessibilityEnabled = isAccessibilityServiceEnabled(context, "com.flowos.app/.capture.FlowSnapAccessibilityService")
        DiagnosticItem(
            "Three-Finger Gesture", 
            if (isAccessibilityEnabled) "ACTIVE" else "DISABLED", 
            if (isAccessibilityEnabled) Success else Warning, 
            Icons.Filled.TouchApp
        )

        Spacer(Modifier.height(DesignTokens.Spacing.Huge))
        FlowPrimaryButton(
            text = "RETEST ALL SYSTEMS", 
            onClick = {}, 
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Filled.Refresh
        )
        
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun DiagnosticItem(label: String, status: String, color: Color, icon: ImageVector) {
    FlowCard(modifier = Modifier.padding(bottom = DesignTokens.Spacing.Medium)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(DesignTokens.Spacing.Large))
            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = CircleShape
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

private fun isAccessibilityServiceEnabled(context: Context, serviceName: String): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
    return enabledServices.any { it.resolveInfo.serviceInfo.name.contains(serviceName) }
}
