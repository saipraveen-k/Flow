package com.flowos.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.ui.ProcessingViewModel

private val PROCESSING_STEPS = listOf(
    "Reading information",
    "Detecting intent",
    "Extracting tasks",
    "Finding deadlines",
    "Connecting context",
    "Building actions",
)

@Composable
fun ProcessingScreen(
    viewModel: ProcessingViewModel,
    onDone: () -> Unit,
    onError: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.startProcessing() }

    LaunchedEffect(state.finished, state.error) {
        if (state.finished) {
            val err = state.error
            if (err != null) onError(err) else onDone()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().animateContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Understanding…", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                text = state.processingLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(48.dp))

            LinearProgressIndicator(
                progress = { (state.stepIndex.toFloat() / PROCESSING_STEPS.size).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).semantics { contentDescription = "Processing progress" },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round
            )
            Spacer(Modifier.height(40.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PROCESSING_STEPS.forEachIndexed { index, step ->
                    val active = index == state.stepIndex
                    val done = index < state.stepIndex
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(if (active) 12.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        done -> MaterialTheme.colorScheme.primary
                                        active -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    }
                                ),
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = step,
                            style = if (active) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                            color = when {
                                done || active -> MaterialTheme.colorScheme.onSurface
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            },
                        )
                    }
                }
            }
        }
    }
}
