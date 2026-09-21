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
import com.flowos.app.ui.components.FlowSectionHeader
import com.flowos.app.ui.theme.FlowAccent

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070707))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(32.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null, tint = Color.White) }
            Text("PRIVACY CENTER", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = Color.White)
        }
        
        Spacer(Modifier.height(32.dp))
        
        PrivacyItem(
            icon = Icons.Filled.Lock,
            title = "Local-First Architecture",
            description = "Your FlowOS data stays on this device. We don't use cloud processing for your personal intents."
        )
        
        Spacer(Modifier.height(24.dp))
        
        PrivacyItem(
            icon = Icons.Filled.VisibilityOff,
            title = "Zero-Insight Policy",
            description = "Screenshots captured via Flow Snap are processed offline using ML Kit and are discarded unless you choose to save them."
        )

        Spacer(Modifier.height(24.dp))
        
        PrivacyItem(
            icon = Icons.Filled.CalendarToday,
            title = "Calendar Control",
            description = "Calendar access is only used to model your capacity. No data is synced to external servers."
        )

        Spacer(Modifier.height(48.dp))
        FlowSectionHeader("DATA MANAGEMENT")
        Spacer(Modifier.height(12.dp))
        
        Button(
            onClick = { /* Clear data logic in settings */ },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f), contentColor = Color.Red)
        ) {
            Text("WIPE ALL LOCAL DATA", fontWeight = FontWeight.Black)
        }
        
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun PrivacyItem(icon: ImageVector, title: String, description: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(FlowAccent.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = FlowAccent)
        }
        Spacer(Modifier.width(20.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color.White)
            Spacer(Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}
