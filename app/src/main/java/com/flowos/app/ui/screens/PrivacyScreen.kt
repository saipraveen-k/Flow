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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.*

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
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
            Text("PRIVACY CENTER", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        }
        
        Spacer(Modifier.height(DesignTokens.Spacing.Huge))
        
        PrivacyItem(
            icon = Icons.Filled.Lock,
            title = "Local-First Architecture",
            description = "Your FlowOS data stays on this device. We don't use cloud processing for your personal intents."
        )
        
        Spacer(Modifier.height(DesignTokens.Spacing.Large))
        
        PrivacyItem(
            icon = Icons.Filled.VisibilityOff,
            title = "Zero-Insight Policy",
            description = "Screenshots captured via Flow Snap are processed offline using ML Kit and are discarded unless you choose to save them."
        )

        Spacer(Modifier.height(DesignTokens.Spacing.Large))
        
        PrivacyItem(
            icon = Icons.Filled.CalendarToday,
            title = "Calendar Control",
            description = "Calendar access is only used to model your capacity. No data is synced to external servers."
        )

        Spacer(Modifier.height(DesignTokens.Spacing.Huge))
        FlowSectionHeader("DATA MANAGEMENT")
        Spacer(Modifier.height(DesignTokens.Spacing.Medium))
        
        FlowSecondaryButton(
            text = "AUDIT ACTIVITY TRAIL",
            onClick = { /* Navigate to activity screen */ },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun PrivacyItem(icon: ImageVector, title: String, description: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(FlowAccent.copy(alpha = 0.1f), RoundedCornerShape(DesignTokens.Shapes.Medium)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = FlowAccent)
        }
        Spacer(Modifier.width(DesignTokens.Spacing.Large))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(DesignTokens.Spacing.Tiny))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
