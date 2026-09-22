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
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.*

@Composable
fun FlowSnapScreen(
    viewModel: FlowSnapViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = DesignTokens.Spacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(DesignTokens.Spacing.Large))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.Close, null) }
            Spacer(Modifier.width(DesignTokens.Spacing.Small))
            Text(
                "FLOW SNAP",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        Spacer(Modifier.height(DesignTokens.Spacing.Huge))

        when (val s = state) {
            FlowSnapUiState.Idle -> {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FlowEmptyState(
                            title = "System Awareness",
                            description = "I'll analyze the information on your screen and turn it into actionable work.",
                            icon = Icons.Filled.Bolt
                        )
                        Spacer(Modifier.height(DesignTokens.Spacing.Section))
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
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.weight(1f)) {
                    Text("ANALYZING REALITY...", style = MaterialTheme.typography.labelLarge, color = FlowAccent, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Spacer(Modifier.height(DesignTokens.Spacing.Large))
                    CircularProgressIndicator(color = FlowAccent, strokeWidth = 2.dp)
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
        // Screenshot Preview (Simulated as Glass for premium feel)
        FlowCard(backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(160.dp)) {
                Icon(Icons.Filled.Image, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(DesignTokens.Spacing.Huge))
                Text("SCAN PREVIEW", color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.BottomCenter))
            }
        }

        Spacer(Modifier.height(DesignTokens.Spacing.Huge))
        Text(
            "EXTRACTED INTELLIGENCE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = FlowAccent,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(DesignTokens.Spacing.Medium))
        Text(
            text = insight.summary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        if (insight.location != null) {
            Spacer(Modifier.height(DesignTokens.Spacing.Small))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Place, null, tint = FlowAccent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(DesignTokens.Spacing.Small))
                Text(insight.location, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(DesignTokens.Spacing.Huge))

        // Actions
        Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.Medium)) {
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
        
        Spacer(Modifier.height(DesignTokens.Spacing.Huge))
    }
}
