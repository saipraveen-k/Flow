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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.action.ActionKind
import com.flowos.app.ui.ExecuteViewModel
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.Success

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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.RocketLaunch, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
        }
        
        Spacer(Modifier.height(24.dp))
        Text("I'M ON IT", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Executing your productivity plan.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(48.dp))

        // ---- EXECUTION CHECKLIST -----------------------------------------
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(24.dp)) {
                state.actions.forEach { action ->
                    val isDone = state.completedKinds.contains(action.kind)
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isDone) Success.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                Icon(Icons.Filled.Check, null, tint = Success, modifier = Modifier.size(14.dp))
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = action.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isDone) FontWeight.Normal else FontWeight.Bold,
                            color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        if (state.failures.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            state.failures.forEach { error ->
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(Modifier.height(48.dp))

        if (state.finished) {
            FlowPrimaryButton(
                text = "CONTINUE TO HOME",
                onClick = onDone,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            FlowLoadingState()
        }
        
        Spacer(Modifier.height(40.dp))
    }
}
