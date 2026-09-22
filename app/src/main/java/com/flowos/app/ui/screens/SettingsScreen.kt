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
import com.flowos.app.settings.ThemeMode
import com.flowos.app.ui.SettingsViewModel
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
) {
    val aiMode by viewModel.aiMode.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    var confirmClear by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DesignTokens.Spacing.Large),
    ) {
        Spacer(Modifier.height(DesignTokens.Spacing.Large))
        Text("SETTINGS", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(DesignTokens.Spacing.Tiny))
        Text(
            "Configure your FlowOS experience.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(DesignTokens.Spacing.Huge))

        // ---- THEME SETTINGS ----------------------------------------------
        FlowSectionHeader("APPEARANCE")
        Spacer(Modifier.height(DesignTokens.Spacing.Medium))
        FlowCard {
            ThemeOption("System Default", themeMode == ThemeMode.SYSTEM_DEFAULT) { viewModel.setThemeMode(ThemeMode.SYSTEM_DEFAULT) }
            Spacer(Modifier.height(DesignTokens.Spacing.Large))
            ThemeOption("Light Theme", themeMode == ThemeMode.LIGHT) { viewModel.setThemeMode(ThemeMode.LIGHT) }
            Spacer(Modifier.height(DesignTokens.Spacing.Large))
            ThemeOption("Dark Theme", themeMode == ThemeMode.DARK) { viewModel.setThemeMode(ThemeMode.DARK) }
        }

        Spacer(Modifier.height(DesignTokens.Spacing.Huge))

        // ---- AI SETTINGS -------------------------------------------------
        FlowSectionHeader("ARTIFICIAL INTELLIGENCE")
        Spacer(Modifier.height(DesignTokens.Spacing.Medium))
        FlowCard {
            Column {
                AiModeOption(
                    title = "Local Intelligence",
                    subtitle = "Deterministic heuristics & on-device analysis.",
                    selected = aiMode == AiMode.LOCAL,
                    onSelect = { viewModel.setAiMode(AiMode.LOCAL) }
                )
                
                Spacer(Modifier.height(DesignTokens.Spacing.Large))
                Surface(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(DesignTokens.Shapes.Small),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "All intelligence is local-first. Your information never leaves your device.",
                        modifier = Modifier.padding(DesignTokens.Spacing.Medium),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(DesignTokens.Spacing.Huge))
        
        // ---- DATA SETTINGS -----------------------------------------------
        FlowSectionHeader("SYSTEM DATA")
        Spacer(Modifier.height(DesignTokens.Spacing.Medium))
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
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(DesignTokens.Shapes.Large)
            )
        }

        Spacer(Modifier.height(DesignTokens.Spacing.Hero))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                "FlowOS v3.0.0 • flagship-edition",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
        Spacer(Modifier.height( DesignTokens.Spacing.Huge))
    }
}

@Composable
private fun ThemeOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
        RadioButton(selected = selected, onClick = onClick)
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
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        RadioButton(
            selected = selected,
            onClick = onSelect
        )
    }
}
