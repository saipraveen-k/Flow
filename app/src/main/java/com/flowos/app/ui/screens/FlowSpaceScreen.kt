package com.flowos.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import com.flowos.app.ui.components.FlowSectionHeader
import com.flowos.app.ui.components.PulseCard
import com.flowos.app.ui.theme.FlowAccent
import com.flowos.app.util.TimeParser

/**
 * FLOW SPACE: Flagship Visual Memory Hub.
 * "Your information, connected to your work."
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
            .background(Color(0xFF070707))
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            "FLOW SPACE",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(16.dp))
        
        // ---- SMART SEARCH: Flagship search experience --------------------
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search captures, outcomes and memory...", style = MaterialTheme.typography.bodyMedium, color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            leadingIcon = { Icon(Icons.Filled.Search, null, tint = FlowAccent) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF101010),
                unfocusedContainerColor = Color(0xFF101010),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(Modifier.height(28.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // ---- SECTION: CATEGORIES -------------------------------------
            FlowSectionHeader("SYSTEM CATEGORIES")
            Spacer(Modifier.height(12.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SpaceCategoryPill(Icons.Filled.PhotoLibrary, "SHOTS", Modifier.weight(1f), onClick = { filePicker.launch("image/*") })
                SpaceCategoryPill(Icons.Filled.Description, "DOCS", Modifier.weight(1f), onClick = { filePicker.launch("application/pdf") })
                SpaceCategoryPill(Icons.Filled.Memory, "DECISIONS", Modifier.weight(1f), onClick = {})
            }

            Spacer(Modifier.height(32.dp))

            // ---- SECTION: RECENT FRAGMENTS -------------------------------
            FlowSectionHeader("RECENT FLOW FRAGMENTS")
            Spacer(Modifier.height(16.dp))
            
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
                    Spacer(Modifier.height(12.dp))
                }
            }

            Spacer(Modifier.height(32.dp))

            // ---- SECTION: RELEVANT OUTCOMES ------------------------------
            FlowSectionHeader("CONNECTED OUTCOMES")
            Spacer(Modifier.height(12.dp))
            
            state.project?.let { project ->
                PulseCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(FlowAccent, CircleShape))
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(project.name.uppercase(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black, color = Color.White)
                            Text("${project.progressPercent}% COMPLETE", style = MaterialTheme.typography.labelSmall, color = FlowAccent, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Filled.ChevronRight, null, tint = Color.Gray)
                    }
                }
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SpaceCategoryPill(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        color = Color(0xFF161616),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = FlowAccent, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun FlagshipMemoryCard(title: String, type: String, timestamp: String) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101010)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
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
                Spacer(Modifier.width(12.dp))
                Text(timestamp, style = MaterialTheme.typography.labelSmall, color = FlowAccent, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = title, 
                style = MaterialTheme.typography.bodyLarge, 
                fontWeight = FontWeight.Bold, 
                color = Color.White,
                maxLines = 3
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = {}, contentPadding = PaddingValues(0.dp)) {
                    Text("CREATE OUTCOME", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = FlowAccent)
                }
                TextButton(onClick = {}, contentPadding = PaddingValues(0.dp)) {
                    Text("ATTACH", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun SpaceEmptyState(onCapture: () -> Unit) {
    PulseCard(accent = false, onClick = onCapture) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.AutoAwesome, null, tint = Color.DarkGray, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(16.dp))
            Text("FLOW SPACE IS EMPTY", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black, color = Color.White)
            Text("Capture intents to populate memory.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}
