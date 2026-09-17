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
import com.flowos.app.ui.theme.FlowAccent
import com.flowos.app.ui.theme.Success

/**
 * EXECUTE: Flagship checklist surface.
 * "I'M ON IT" - executing real Android actions truthfully.
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
            .background(Color(0xFF070707))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(64.dp))
        
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
                modifier = Modifier.size(48.dp)
            )
        }
        
        Spacer(Modifier.height(32.dp))
        Text(
            "I'M ON IT", 
            style = MaterialTheme.typography.headlineMedium, 
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Executing your flagship productivity plan.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(64.dp))

        // ---- EXECUTION CHECKLIST: Premium checklist items ----------------
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101010)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(28.dp)) {
                state.actions.forEach { action ->
                    val isDone = state.completedKinds.contains(action.kind)
                    Row(
                        modifier = Modifier.padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isDone) Success.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                Icon(Icons.Filled.Check, null, tint = Success, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(Modifier.width(20.dp))
                        Text(
                            text = action.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isDone) FontWeight.Medium else FontWeight.Black,
                            color = if (isDone) Color.Gray else Color.White
                        )
                    }
                }
            }
        }

        if (state.failures.isNotEmpty()) {
            Spacer(Modifier.height(32.dp))
            state.failures.forEach { error ->
                Text(
                    error, 
                    color = MaterialTheme.colorScheme.error, 
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(64.dp))

        if (state.finished) {
            FlowPrimaryButton(
                text = "CONTINUE TO ATTENTION CENTER",
                onClick = onDone,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            FlowLoadingState()
        }
        
        Spacer(Modifier.height(60.dp))
    }
}
