package com.flowos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.settings.AiMode
import com.flowos.app.ui.SettingsViewModel
import com.flowos.app.ui.components.*

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
) {
    val aiMode by viewModel.aiMode.collectAsStateWithLifecycle()
    var confirmClear by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070707))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(32.dp))
        Text("SETTINGS", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color.White)
        Spacer(Modifier.height(4.dp))
        Text(
            "Configure your FlowOS experience.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
        )
        Spacer(Modifier.height(32.dp))

        FlowSectionHeader("ARTIFICIAL INTELLIGENCE")
        Spacer(Modifier.height(12.dp))
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101010)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(24.dp)) {
                AiModeOption(
                    title = "Local Intelligence",
                    subtitle = "Deterministic heuristics & on-device analysis.",
                    selected = aiMode == AiMode.LOCAL,
                    onSelect = { viewModel.setAiMode(AiMode.LOCAL) }
                )
                
                Spacer(Modifier.height(24.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "All intelligence is local-first. Your information never leaves your device.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(Modifier.height(40.dp))
        FlowSectionHeader("SYSTEM DATA")
        Spacer(Modifier.height(12.dp))
        FlowSecondaryButton(
            text = "WIPE ALL LOCAL DATA",
            onClick = { confirmClear = true },
            modifier = Modifier.fillMaxWidth()
        )

        if (confirmClear) {
            AlertDialog(
                onDismissRequest = { confirmClear = false },
                title = { Text("Erase all data?") },
                text = { Text("This will permanently remove all tasks, captures, and workflows from your phone.") },
                confirmButton = {
                    TextButton(onClick = {
                        confirmClear = false
                        viewModel.clearAllData { onBack() }
                    }) {
                        Text("ERASE", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmClear = false }) {
                        Text("CANCEL")
                    }
                },
                containerColor = Color(0xFF161616),
                shape = RoundedCornerShape(28.dp)
            )
        }

        Spacer(Modifier.height(48.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                "FlowOS v0.1.0 • flagship-edition",
                style = MaterialTheme.typography.labelSmall,
                color = Color.DarkGray
            )
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun AiModeOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onSelect() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        RadioButton(
            selected = selected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
        )
    }
}
