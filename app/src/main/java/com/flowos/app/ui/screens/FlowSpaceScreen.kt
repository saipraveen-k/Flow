package com.flowos.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.ui.ActivityViewModel
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.*
import com.flowos.app.util.TimeParser

/**
 * FLOW SPACE: Flagship Visual Memory Hub.
 */
@Composable
fun FlowSpaceScreen(
    viewModel: ActivityViewModel,
    onNavigateToCapture: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        // Logic to store/link file
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = DesignTokens.Spacing.Large),
    ) {
        Spacer(Modifier.height(DesignTokens.Spacing.Large))
        Text(
            "FLOW SPACE",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(DesignTokens.Spacing.Small))
        Text(
            "Your information, connected to your work.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(DesignTokens.Spacing.Huge))
        
        // ---- SMART SEARCH ----
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search captures and memory...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(DesignTokens.Shapes.Medium),
            leadingIcon = { Icon(Icons.Filled.Search, null, tint = FlowAccent) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        )

        Spacer(Modifier.height(DesignTokens.Spacing.Huge))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            FlowSectionHeader("SYSTEM CATEGORIES")
            Spacer(Modifier.height(DesignTokens.Spacing.Medium))
            
            Row(horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.Medium)) {
                SpaceCategoryPill(Icons.Filled.PhotoLibrary, "SHOTS", Modifier.weight(1f), onClick = { filePicker.launch("image/*") })
                SpaceCategoryPill(Icons.Filled.Description, "DOCS", Modifier.weight(1f), onClick = { filePicker.launch("application/pdf") })
            }

            Spacer(Modifier.height(DesignTokens.Spacing.Huge))
            FlowSectionHeader("RECENT FRAGMENTS")
            Spacer(Modifier.height(DesignTokens.Spacing.Medium))
            
            if (state.recentCaptures.isEmpty()) {
                SpaceEmptyState(onNavigateToCapture)
            } else {
                state.recentCaptures.forEach { capture ->
                    val timestampLabel = remember(capture.createdAt) {
                        TimeParser.humanLabel(capture.createdAt)
                    }
                    FlagshipMemoryCard(
                        title = capture.summary ?: capture.rawText.take(80),
                        type = capture.sourceType,
                        timestamp = timestampLabel
                    )
                    Spacer(Modifier.height(DesignTokens.Spacing.Medium))
                }
            }

            Spacer(Modifier.height(DesignTokens.Spacing.Huge))
            FlowSectionHeader("CONNECTED OUTCOMES")
            Spacer(Modifier.height(DesignTokens.Spacing.Medium))
            
            state.activeOutcomes.take(1).forEach { outcome ->
                FlowCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(FlowAccent, CircleShape))
                        Spacer(Modifier.width(DesignTokens.Spacing.Large))
                        Column(Modifier.weight(1f)) {
                            Text(outcome.title.uppercase(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black)
                            Text("${outcome.progressPercent}% COMPLETE", style = MaterialTheme.typography.labelSmall, color = FlowAccent, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
                    }
                }
            }
            
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun SpaceCategoryPill(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(DesignTokens.Shapes.Medium),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = DesignTokens.Spacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = FlowAccent, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun FlagshipMemoryCard(title: String, type: String, timestamp: String) {
    FlowCard {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(FlowAccent.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when(type) {
                            "VOICE" -> Icons.Filled.Mic
                            "IMAGE" -> Icons.Filled.CameraAlt
                            "DOCUMENT" -> Icons.Filled.Description
                            else -> Icons.Filled.EditNote
                        },
                        contentDescription = null,
                        tint = FlowAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.width(DesignTokens.Spacing.Medium))
                Text(timestamp, style = MaterialTheme.typography.labelSmall, color = FlowAccent, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(DesignTokens.Spacing.Medium))
            Text(
                text = title, 
                style = MaterialTheme.typography.bodyLarge, 
                fontWeight = FontWeight.Bold, 
                maxLines = 3
            )
            Spacer(Modifier.height(DesignTokens.Spacing.Large))
            Row(horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.Large)) {
                Text(
                    text = "ATTACH", 
                    style = MaterialTheme.typography.labelSmall, 
                    fontWeight = FontWeight.Black, 
                    color = FlowAccent,
                    modifier = Modifier.clickable {  }
                )
                Text(
                    text = "DISCARD", 
                    style = MaterialTheme.typography.labelSmall, 
                    fontWeight = FontWeight.Black, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable {  }
                )
            }
        }
    }
}

@Composable
private fun SpaceEmptyState(onCapture: () -> Unit) {
    FlowCard(onClick = onCapture, backgroundColor = Color.Transparent) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.AutoAwesome, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(DesignTokens.Spacing.Medium))
            Text("FLOW SPACE IS EMPTY", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black)
            Text("Capture intents to populate memory.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
