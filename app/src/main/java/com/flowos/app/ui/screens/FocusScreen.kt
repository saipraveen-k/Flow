package com.flowos.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.domain.model.TaskStatus
import com.flowos.app.ui.FocusViewModel
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.Success

@Composable
fun FocusScreen(
    viewModel: FocusViewModel,
    onExit: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        
        // ---- HEADER: Objective -------------------------------------------
        Text(
            text = state.projectName?.uppercase() ?: "FOCUS",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(48.dp))

        if (state.finished || state.currentTask == null) {
            FocusCompleteState(onExit)
            return@Column
        }

        val task = state.currentTask!!

        // ---- VISUAL TIMER: Large & Technical -----------------------------
        LargeTimer(
            elapsedSeconds = state.elapsedSeconds,
            estimatedMinutes = task.estimatedDurationMinutes,
            isActive = state.timerActive
        )

        Spacer(Modifier.height(48.dp))

        // ---- TASK DETAILS ------------------------------------------------
        Text(
            text = task.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        if (task.description.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(48.dp))

        // ---- ACTIONS: Start/Pause & Complete -----------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.timerActive) {
                FlowSecondaryButton(
                    text = "PAUSE",
                    onClick = { viewModel.stopTimer() },
                    modifier = Modifier.weight(1f)
                )
            } else {
                FlowPrimaryButton(
                    text = "START",
                    onClick = { 
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.startTimer() 
                    },
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.PlayArrow
                )
            }
            
            FlowPrimaryButton(
                text = "DONE",
                onClick = { 
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.completeCurrent() 
                },
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Check
            )
        }

        // ---- OUTCOME PROOF: Contextual -----------------------------------
        if (task.status == TaskStatus.DONE.name && !state.proofAttached) {
            Spacer(Modifier.height(32.dp))
            OutcomeProofCard(
                onAttachScreenshot = { viewModel.attachProof("SCREENSHOT") },
                onAttachFile = { viewModel.attachProof("FILE") },
                onConfirm = { viewModel.attachProof("CONFIRMATION") }
            )
        }

        Spacer(Modifier.height(32.dp))
        TextButton(onClick = { viewModel.skipCurrent() }) {
            Text("SKIP THIS TASK", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun LargeTimer(
    elapsedSeconds: Long,
    estimatedMinutes: Int,
    isActive: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "timerPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(240.dp)
            .scale(if (isActive) pulseScale else 1f)
            .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape)
    ) {
        // Simple progress ring
        val progress = (elapsedSeconds.toFloat() / (estimatedMinutes * 60)).coerceAtMost(1f)
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(220.dp),
            strokeWidth = 8.dp,
            trackColor = Color.Transparent,
            strokeCap = StrokeCap.Round
        )
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatSeconds(elapsedSeconds),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = if (elapsedSeconds > estimatedMinutes * 60) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isActive) "ACTIVE FOCUS" else "PAUSED",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun OutcomeProofCard(
    onAttachScreenshot: () -> Unit,
    onAttachFile: () -> Unit,
    onConfirm: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("PROVE OUTCOME", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProofOption(Icons.Filled.PhotoCamera, "SHOT", onAttachScreenshot)
                ProofOption(Icons.Filled.Description, "FILE", onAttachFile)
                ProofOption(Icons.Filled.Verified, "CONFIRM", onConfirm)
            }
        }
    }
}

@Composable
private fun RowScope.ProofOption(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FocusCompleteState(onExit: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            Icons.Filled.Verified, 
            null, 
            tint = Success,
            modifier = Modifier.size(80.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text("OUTCOME ACHIEVED", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(8.dp))
        Text(
            "Every step of this thread is verified.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(48.dp))
        FlowPrimaryButton(text = "DONE", onClick = onExit, modifier = Modifier.fillMaxWidth())
    }
}

private fun formatSeconds(totalSeconds: Long): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%02d:%02d".format(m, s)
}
