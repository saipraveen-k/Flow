package com.flowos.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.ui.ProcessingViewModel
import com.flowos.app.ui.theme.FlowAccent

@Composable
fun ProcessingScreen(
    viewModel: ProcessingViewModel,
    onDone: () -> Unit,
    onError: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.startProcessing()
    }

    LaunchedEffect(state.finished) {
        if (state.finished) {
            if (state.error == null) onDone() else onError()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF070707)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 48.dp)
        ) {
            // ---- FLAGSHIP INTELLIGENCE ANIMATION -------------------------
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(100.dp),
                    color = FlowAccent,
                    strokeWidth = 2.dp,
                    trackColor = Color.Transparent
                )
                CircularProgressIndicator(
                    modifier = Modifier.size(80.dp),
                    color = FlowAccent.copy(alpha = 0.3f),
                    strokeWidth = 8.dp,
                    trackColor = Color.Transparent,
                    strokeCap = StrokeCap.Round
                )
                Icon(
                    Icons.Filled.Settings,
                    null, 
                    tint = FlowAccent, 
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(Modifier.height(48.dp))
            
            AnimatedContent(
                targetState = state.stepIndex,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "intelligenceSteps"
            ) { index ->
                Text(
                    text = getProcessingStepLabel(index),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }
            
            Spacer(Modifier.height(16.dp))
            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = CircleShape
            ) {
                Text(
                    text = state.processingLabel.uppercase(),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = FlowAccent,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

private fun getProcessingStepLabel(index: Int): String = when (index) {
    0 -> "UNDERSTANDING INTENT"
    1 -> "CONNECTING CONTEXT"
    2 -> "OPTIMIZING PLAN"
    3 -> "DETECTING FRICTION"
    else -> "FINALIZING FLOW"
}
