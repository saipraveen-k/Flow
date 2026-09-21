package com.flowos.app.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.capture.FlowSnapIntelligenceEngine
import com.flowos.app.capture.FlowSnapUiState
import com.flowos.app.capture.FlowSnapViewModel
import com.flowos.app.domain.model.AIAnalysisResult
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.FlowAccent

@Composable
fun FlowSnapScreen(
    viewModel: FlowSnapViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070707))
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.Close, null, tint = Color.White) }
            Spacer(Modifier.width(8.dp))
            Text(
                "FLOW SNAP",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp
            )
        }

        Spacer(Modifier.height(24.dp))

        when (val s = state) {
            FlowSnapUiState.Idle -> {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FlowEmptyState(
                            title = "Ready to Snap",
                            description = "I'll analyze the information on your screen and turn it into action.",
                            icon = Icons.Filled.Bolt
                        )
                        Spacer(Modifier.height(32.dp))
                        FlowPrimaryButton(
                            text = "INITIATE SCAN",
                            onClick = { 
                                viewModel.onScreenshotCaptured(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
                            }
                        )
                    }
                }
            }
            FlowSnapUiState.Capturing -> {
                FlowLoadingState()
            }
            is FlowSnapUiState.Analyzing -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ANALYZING SCREEN...", style = MaterialTheme.typography.labelLarge, color = FlowAccent, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(24.dp))
                    CircularProgressIndicator(color = FlowAccent)
                }
            }
            is FlowSnapUiState.Success -> {
                FlowSnapResultContent(
                    insight = s.insight, 
                    screenshot = s.screenshot, 
                    onSave = { viewModel.saveToFlowSpace(s.insight) },
                    onAction = { action ->
                        when (action) {
                            "ADD TO CALENDAR" -> viewModel.addToCalendar(s.insight)
                            "SET REMINDER", "REMIND ME" -> viewModel.setReminder(s.insight)
                        }
                    }
                )
            }
            is FlowSnapUiState.Error -> {
                FlowErrorState(message = s.message, onRetry = { onBack() })
            }
        }
    }
}

@Composable
private fun FlowSnapResultContent(
    insight: FlowSnapIntelligenceEngine.SnapInsight,
    screenshot: Bitmap,
    onSave: () -> Unit,
    onAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Screenshot Preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(Icons.Filled.Image, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                Text("SCREENSHOT PREVIEW", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(Modifier.height(32.dp))
        Text(
            "HERE'S WHAT MATTERS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = FlowAccent,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = insight.summary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        if (insight.location != null) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Place, null, tint = FlowAccent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(insight.location, color = Color.White, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(Modifier.height(32.dp))

        // Actions
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            insight.suggestedActions.forEach { action ->
                FlowPrimaryButton(
                    text = action,
                    onClick = { onAction(action) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            FlowSecondaryButton(
                text = "SAVE TO FLOW SPACE",
                onClick = onSave,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(Modifier.height(48.dp))
    }
}
