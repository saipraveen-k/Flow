package com.flowos.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.FlowAccent

/**
 * FLOW MEMORY: "Your work remembers the context."
 * Flagship surface for project decisions, questions, and context.
 */
@Composable
fun MemoryScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070707))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null, tint = Color.White) }
            Text(
                "FLOW MEMORY", 
                style = MaterialTheme.typography.headlineSmall, 
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Persistent context for your outcomes.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            modifier = Modifier.padding(start = 12.dp)
        )
        
        Spacer(Modifier.height(32.dp))

        // ---- PROJECT CONTEXT: Active focus ------------------------------
        FlowSectionHeader("ACTIVE CONTEXT")
        Spacer(Modifier.height(12.dp))
        PulseCard(accent = true) {
            Text("HACKATHON DEMO", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text("Adaptive Personal Work OS prototype for flagship submission.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }

        Spacer(Modifier.height(32.dp))
        
        FlagshipMemorySection(
            title = "DECISIONS",
            icon = Icons.Filled.Verified,
            items = listOf("Using local-first architecture for MVP", "Kinetic yellow as primary interaction accent")
        )

        Spacer(Modifier.height(24.dp))
        FlagshipMemorySection(
            title = "OPEN QUESTIONS",
            icon = Icons.Filled.QuestionAnswer,
            items = listOf("Haptic feedback strength for capture", "NPU acceleration roadmap")
        )

        Spacer(Modifier.height(24.dp))
        FlagshipMemorySection(
            title = "IMPORTANT CONTEXT",
            icon = Icons.Filled.Info,
            items = listOf("Targeting iQOO flagship experience", "Zero-network privacy guarantee")
        )

        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun FlagshipMemorySection(title: String, icon: ImageVector, items: List<String>) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = FlowAccent, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(12.dp))
            FlowSectionHeader(title)
        }
        Spacer(Modifier.height(12.dp))
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101010)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(24.dp)) {
                items.forEach { item ->
                    Row(modifier = Modifier.padding(vertical = 6.dp)) {
                        Text("•", style = MaterialTheme.typography.bodyLarge, color = FlowAccent, fontWeight = FontWeight.Black)
                        Spacer(Modifier.width(12.dp))
                        Text(item, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    }
                }
            }
        }
    }
}
