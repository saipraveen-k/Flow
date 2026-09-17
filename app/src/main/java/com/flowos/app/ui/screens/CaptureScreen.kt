package com.flowos.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
        Spacer(Modifier.height(24.dp))
        FlowSectionHeader("CAPTURE")
        Spacer(Modifier.height(8.dp))
        Text("WHAT DO YOU WANT TO ACCOMPLISH?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(32.dp))

        // ---- QUICK TEXT INPUT --------------------------------------------
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(24.dp)) {
                TextField(
                    value = typedText,
                    onValueChange = { typedText = it },
                    placeholder = { Text("Type an outcome or requirement...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(16.dp))
                FlowPrimaryButton(
                    text = "PROCESS TEXT",
                    onClick = { viewModel.submitTypedText(typedText) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = typedText.isNotBlank()
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        FlowSectionHeader("OTHER METHODS")
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CaptureMethodTile(Icons.Filled.Mic, "VOICE", Modifier.weight(1f)) { viewModel.startVoiceCapture() }
            CaptureMethodTile(Icons.Filled.CameraAlt, "CAMERA", Modifier.weight(1f)) { imageLauncher.launch("image/*") }
        }
        Spacer(Modifier.height(16.dp))
        CaptureMethodTile(Icons.Filled.Description, "DOCUMENT", Modifier.fillMaxWidth()) { docLauncher.launch("*/*") }

        if (state is CaptureUiState.Listening) {
            Spacer(Modifier.height(32.dp))
            ListeningOverlay(onStop = { viewModel.stopVoiceCapture() })
        }

        if (state is CaptureUiState.Error) {
            Spacer(Modifier.height(16.dp))
            Text((state as CaptureUiState.Error).message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun CaptureMethodTile(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun ListeningOverlay(onStop: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("LISTENING...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            FlowPrimaryButton(text = "STOP", onClick = onStop, modifier = Modifier.fillMaxWidth())
        }
    }
}
