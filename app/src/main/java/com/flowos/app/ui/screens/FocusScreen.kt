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
import com.flowos.app.ui.theme.*

/**
 * FOCUS MODE: Immersive Flagship Execution Surface.
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

    val backgroundColor by animateColorAsState(
        targetValue = if (state.timerActive) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f) else MaterialTheme.colorScheme.background,
        animationSpec = tween(DesignTokens.Animation.Slow)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DesignTokens.Spacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(DesignTokens.Spacing.Hero))
        
        Text(
            text = state.projectName?.uppercase() ?: "FOCUS SESSION",
            style = MaterialTheme.typography.labelLarge,
            color = FlowAccent,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(DesignTokens.Spacing.Huge))

        if (state.finished || state.currentTask == null) {
            FocusCompleteState(onExit)
            return@Column
        }

        val task = state.currentTask!!

        TechnicalKineticTimer(
            elapsedSeconds = state.elapsedSeconds,
            estimatedMinutes = task.estimatedDurationMinutes,
            isActive = state.timerActive
        )

        Spacer(Modifier.height(DesignTokens.Spacing.Hero))

        Text(
            text = task.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp
        )
        if (task.description.isNotBlank()) {
            Spacer(Modifier.height(DesignTokens.Spacing.Medium))
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(DesignTokens.Spacing.Hero))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.Medium)
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

        if (task.status == TaskStatus.DONE.name && !state.proofAttached) {
            Spacer(Modifier.height(DesignTokens.Spacing.Huge))
            PremiumProofHub(
                onAttachShot = { imageLauncher.launch("image/*") },
                onAttachFile = { fileLauncher.launch("*/*") },
                onConfirm = { viewModel.attachProof("CONFIRMATION") }
            )
        }

        Spacer(Modifier.height(DesignTokens.Spacing.Huge))
        TextButton(onClick = { viewModel.skipCurrent() }) {
            Text("SKIP THIS TASK", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
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
            animation = tween(DesignTokens.Animation.Slow * 3, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(280.dp)
    ) {
        val progress = (elapsedSeconds.toFloat() / (estimatedMinutes * 60)).coerceAtMost(1f)
        
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.size(280.dp),
            strokeWidth = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
            trackColor = Color.Transparent
        )

        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(260.dp),
            strokeWidth = 12.dp,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f),
            strokeCap = StrokeCap.Round,
            color = if (elapsedSeconds > estimatedMinutes * 60) MaterialTheme.colorScheme.error else FlowAccent
        )
        
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
                color = if (elapsedSeconds > estimatedMinutes * 60) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-2).sp
            )
            Text(
                text = if (isActive) "ACTIVE FLOW" else "PAUSED",
                style = MaterialTheme.typography.labelMedium,
                color = if (isActive) FlowAccent else MaterialTheme.colorScheme.onSurfaceVariant,
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
    FlowCard {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "VERIFY THE OUTCOME", 
                style = MaterialTheme.typography.labelSmall, 
                fontWeight = FontWeight.Black, 
                letterSpacing = 2.sp, 
                color = FlowAccent
            )
            Spacer(Modifier.height(DesignTokens.Spacing.Large))
            Row(horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.Medium)) {
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
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        shape = RoundedCornerShape(DesignTokens.Shapes.Medium),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.Spacing.Medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, modifier = Modifier.size(24.dp), tint = FlowAccent)
            Spacer(Modifier.height(DesignTokens.Spacing.Small))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
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
        Spacer(Modifier.height(DesignTokens.Spacing.Hero))
        Text("GOAL ACHIEVED", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(DesignTokens.Spacing.Small))
        Text(
            "Every step has been verified.\nYour performance is recorded.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(DesignTokens.Spacing.Hero))
        FlowPrimaryButton(text = "FINISH SESSION", onClick = onExit, modifier = Modifier.fillMaxWidth())
    }
}

private fun formatSeconds(totalSeconds: Long): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%02d:%02d".format(m, s)
}
