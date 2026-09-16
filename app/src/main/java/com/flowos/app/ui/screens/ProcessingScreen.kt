package com.flowos.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.ui.ProcessingViewModel
import com.flowos.app.ui.components.FlowSectionHeader

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
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 48.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 6.dp
            )
            Spacer(Modifier.height(32.dp))
            
            AnimatedContent(
                targetState = state.stepIndex,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "processingStep"
            ) { index ->
                Text(
                    text = getProcessingStepLabel(index),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }
            
            Spacer(Modifier.height(12.dp))
            Text(
                text = state.processingLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun getProcessingStepLabel(index: Int): String = when (index) {
    0 -> "UNDERSTANDING YOUR INPUT..."
    1 -> "BUILDING YOUR WORK GRAPH..."
    2 -> "CHECKING AVAILABLE TIME..."
    3 -> "ANALYZING PLAN RISK..."
    else -> "FINALIZING OUTCOME..."
}
