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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowos.app.ui.components.*

@Composable
fun MemoryScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) }
            Text("FLOW MEMORY", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Lightweight work-context memory.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp)
        )
        
        Spacer(Modifier.height(24.dp))

        FlowSectionHeader("PROJECT CONTEXT")
        Spacer(Modifier.height(12.dp))
        PulseCard(accent = true) {
            Text("HACKATHON DEMO", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            Text("Adaptive Personal Work OS prototype for submission.", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(28.dp))
        MemorySection(
            title = "DECISIONS",
            items = listOf("Using FlowOS adaptive loop for demo", "Dark surface UI with yellow accent")
        )

        Spacer(Modifier.height(20.dp))
        MemorySection(
            title = "OPEN QUESTIONS",
            items = listOf("Final demo environment on Pixel 7", "NPU acceleration verification")
        )

        Spacer(Modifier.height(20.dp))
        MemorySection(
            title = "DEPENDENCIES",
            items = listOf("API integration before testing", "Outcome compiler validation")
        )

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun MemorySection(title: String, items: List<String>) {
    Column {
        FlowSectionHeader(title)
        Spacer(Modifier.height(12.dp))
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {
                items.forEach { item ->
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text("•", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                        Spacer(Modifier.width(12.dp))
                        Text(item, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
