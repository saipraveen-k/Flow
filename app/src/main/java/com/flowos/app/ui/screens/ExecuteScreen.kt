package com.flowos.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RocketLaunch
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
import com.flowos.app.action.ActionKind
import com.flowos.app.ui.ExecuteViewModel
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.*

/**
 * EXECUTE: Flagship checklist surface.
 */
@Composable
fun ExecuteScreen(
    viewModel: ExecuteViewModel,
    onDone: () -> Unit,
    onEdit: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.prepare()
        viewModel.execute()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DesignTokens.Spacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(DesignTokens.Spacing.Hero))
        
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(FlowAccent.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.RocketLaunch, 
                null, 
                tint = FlowAccent, 
                modifier = Modifier.size(DesignTokens.Spacing.Hero + 8.dp)
            )
        }
        
        Spacer(Modifier.height(DesignTokens.Spacing.Large))
        Text(
            "I'M ON IT", 
            style = MaterialTheme.typography.headlineMedium, 
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(DesignTokens.Spacing.Small))
        Text(
            text = "Executing your flagship productivity plan.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(DesignTokens.Spacing.Huge))

        // ---- EXECUTION CHECKLIST ----
        FlowCard {
            Column {
                state.actions.forEach { action ->
                    val isDone = state.completedKinds.contains(action.kind)
                    Row(
                        modifier = Modifier.padding(vertical = DesignTokens.Spacing.Small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isDone) Success.copy(alpha = 0.1f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                Icon(Icons.Filled.Check, null, tint = Success, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(Modifier.width(DesignTokens.Spacing.Medium))
                        Text(
                            text = action.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isDone) FontWeight.Medium else FontWeight.Black,
                            color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        if (state.failures.isNotEmpty()) {
            Spacer(Modifier.height(DesignTokens.Spacing.Large))
            state.failures.forEach { error ->
                Text(
                    error, 
                    color = MaterialTheme.colorScheme.error, 
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(DesignTokens.Spacing.Huge))

        if (state.finished) {
            FlowPrimaryButton(
                text = "FINISH",
                onClick = onDone,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            FlowLoadingState()
        }
        
        Spacer(Modifier.height(80.dp))
    }
}
