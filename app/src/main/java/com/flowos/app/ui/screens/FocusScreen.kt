package com.flowos.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import com.flowos.app.ui.theme.FlowAccent
import com.flowos.app.ui.theme.Success

/**
 * FOCUS MODE: Immersive Flagship Execution Surface.
 * No navigation. No clutter. Just technical focus and verification.
 */
@Composable
fun FocusScreen(
    viewModel: FocusViewModel,
    onExit: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.attachProof("SCREENSHOT", it.toString()) }
    }
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.attachProof("FILE", it.toString()) }
    }

    // Flagship background: subtle breathing color based on state
    val backgroundColor by animateColorAsState(
        targetValue = if (state.timerActive) Color(0xFF0C0C0C) else Color(0xFF070707),
        animationSpec = tween(1000)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        
        // ---- HEADER: Contextual Thread -----------------------------------
        Text(
            text = state.projectName?.uppercase() ?: "FOCUS SESSION",
            style = MaterialTheme.typography.labelLarge,
            color = FlowAccent,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(60.dp))

        if (state.finished || state.currentTask == null) {
            FocusCompleteState(onExit)
            return@Column
        }

        val task = state.currentTask!!

        // ---- KINETIC TIMER: Large pulsing flagship ring ------------------
        TechnicalKineticTimer(
            elapsedSeconds = state.elapsedSeconds,
            estimatedMinutes = task.estimatedDurationMinutes,
            isActive = state.timerActive
        )

        Spacer(Modifier.height(60.dp))

        // ---- TASK TITLE: High-Contrast Hero -----------------------------
        Text(
            text = task.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            color = Color.White,
            letterSpacing = (-0.5).sp
        )
        if (task.description.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(64.dp))

        // ---- CORE ACTIONS: Execution Controls ----------------------------
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
                text = "COMPLETE",
                onClick = { 
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.completeCurrent() 
                },
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Check
            )
        }

        // ---- OUTCOME PROOF: Flagship Verification Hub --------------------
        if (task.status == TaskStatus.DONE.name && !state.proofAttached) {
            Spacer(Modifier.height(48.dp))
            PremiumProofHub(
                onAttachShot = { imageLauncher.launch("image/*") },
                onAttachFile = { fileLauncher.launch("*/*") },
                onConfirm = { viewModel.attachProof("CONFIRMATION") }
            )
        }

        Spacer(Modifier.height(48.dp))
        TextButton(onClick = { viewModel.skipCurrent() }) {
            Text("SKIP THIS TASK", style = MaterialTheme.typography.labelLarge, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(60.dp))
    }
}

@Composable
private fun TechnicalKineticTimer(
    elapsedSeconds: Long,
    estimatedMinutes: Int,
    isActive: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "timerBreathing")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(280.dp)
    ) {
        val progress = (elapsedSeconds.toFloat() / (estimatedMinutes * 60)).coerceAtMost(1f)
        
        // Outer technical track
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.size(280.dp),
            strokeWidth = 1.dp,
            color = Color.White.copy(alpha = 0.05f),
            trackColor = Color.Transparent
        )

        // Primary progress ring
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(260.dp),
            strokeWidth = 12.dp,
            trackColor = Color.White.copy(alpha = 0.02f),
            strokeCap = StrokeCap.Round,
            color = if (elapsedSeconds > estimatedMinutes * 60) MaterialTheme.colorScheme.error else FlowAccent
        )
        
        // Active pulse ring
        if (isActive) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(260.dp).scale(1.05f),
                strokeWidth = 2.dp,
                color = FlowAccent.copy(alpha = alpha * 0.2f),
                trackColor = Color.Transparent
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatSeconds(elapsedSeconds),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Black,
                color = if (elapsedSeconds > estimatedMinutes * 60) MaterialTheme.colorScheme.error else Color.White,
                letterSpacing = (-2).sp
            )
            Text(
                text = if (isActive) "ACTIVE FLOW" else "PAUSED",
                style = MaterialTheme.typography.labelMedium,
                color = if (isActive) FlowAccent else Color.Gray,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp
            )
        }
    }
}

@Composable
private fun PremiumProofHub(
    onAttachShot: () -> Unit,
    onAttachFile: () -> Unit,
    onConfirm: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101010)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "VERIFY THE OUTCOME", 
                style = MaterialTheme.typography.labelSmall, 
                fontWeight = FontWeight.Black, 
                letterSpacing = 2.sp, 
                color = FlowAccent
            )
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ProofHubOption(Icons.Filled.PhotoCamera, "SHOT", onAttachShot)
                ProofHubOption(Icons.Filled.Description, "FILE", onAttachFile)
                ProofHubOption(Icons.Filled.Verified, "YES", onConfirm)
            }
        }
    }
}

@Composable
private fun RowScope.ProofHubOption(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, modifier = Modifier.size(24.dp), tint = FlowAccent)
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.White)
        }
    }
}

@Composable
private fun FocusCompleteState(onExit: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Success.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Verified, 
                null, 
                tint = Success,
                modifier = Modifier.size(80.dp)
            )
        }
        Spacer(Modifier.height(40.dp))
        Text("GOAL ACHIEVED", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color.White)
        Spacer(Modifier.height(12.dp))
        Text(
            "Every step has been verified.\nYour performance is recorded.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(64.dp))
        FlowPrimaryButton(text = "FINISH SESSION", onClick = onExit, modifier = Modifier.fillMaxWidth())
    }
}

private fun formatSeconds(totalSeconds: Long): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%02d:%02d".format(m, s)
}
