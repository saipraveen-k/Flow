package com.flowos.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.ui.CaptureUiState
import com.flowos.app.ui.CaptureViewModel
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.FlowAccent

@Composable
fun CaptureScreen(
    viewModel: CaptureViewModel,
    onReadyToProcess: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var typedText by remember { mutableStateOf("") }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.onImageCaptured(it) }
    }
    val docLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.onDocumentSelected(it) }
    }

    LaunchedEffect(state) {
        if (state is CaptureUiState.Ready) {
            onReadyToProcess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(32.dp))
        FlowSectionHeader("CAPTURE")
        Spacer(Modifier.height(8.dp))
        Text(
            "WHAT DO YOU WANT TO ACCOMPLISH?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = (-0.5).sp
        )
        Spacer(Modifier.height(32.dp))

        // ---- TEXT INPUT SURFACE ------------------------------------------
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101010)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(28.dp)) {
                TextField(
                    value = typedText,
                    onValueChange = { typedText = it },
                    placeholder = { 
                        Text(
                            "Type your goal, requirement, or intent...", 
                            style = MaterialTheme.typography.bodyLarge, 
                            color = Color.Gray
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                )
                Spacer(Modifier.height(24.dp))
                FlowPrimaryButton(
                    text = "ANALYZE INTENT",
                    onClick = { viewModel.submitTypedText(typedText) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = typedText.isNotBlank()
                )
            }
        }

        Spacer(Modifier.height(32.dp))
        FlowSectionHeader("SYSTEM MODES")
        Spacer(Modifier.height(16.dp))

        // ---- KINETIC MODE TILES ------------------------------------------
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            FlagshipModeTile(
                icon = Icons.Filled.Mic, 
                label = "VOICE", 
                modifier = Modifier.weight(1f),
                onClick = { viewModel.startVoiceCapture() }
            )
            FlagshipModeTile(
                icon = Icons.Filled.CameraAlt, 
                label = "CAMERA", 
                modifier = Modifier.weight(1f),
                onClick = { imageLauncher.launch("image/*") }
            )
        }
        Spacer(Modifier.height(16.dp))
        FlagshipModeTile(
            icon = Icons.Filled.Description, 
            label = "DOCUMENT", 
            modifier = Modifier.fillMaxWidth(),
            onClick = { docLauncher.launch("*/*") }
        )

        if (state is CaptureUiState.Listening) {
            Spacer(Modifier.height(48.dp))
            FlagshipListeningOverlay(onStop = { viewModel.stopVoiceCapture() })
        }

        if (state is CaptureUiState.Error) {
            Spacer(Modifier.height(24.dp))
            Text(
                (state as CaptureUiState.Error).message, 
                color = MaterialTheme.colorScheme.error, 
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(Modifier.height(60.dp))
    }
}

@Composable
private fun FlagshipModeTile(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        color = Color(0xFF161616),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(FlowAccent.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = FlowAccent, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = label, 
                style = MaterialTheme.typography.labelMedium, 
                fontWeight = FontWeight.Black, 
                color = Color.White,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun FlagshipListeningOverlay(onStop: () -> Unit) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = FlowAccent.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, FlowAccent.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "LISTENING", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Black, 
                color = FlowAccent,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "I'm capturing your intent...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Spacer(Modifier.height(32.dp))
            FlowPrimaryButton(text = "STOP", onClick = onStop, modifier = Modifier.fillMaxWidth())
        }
    }
}
