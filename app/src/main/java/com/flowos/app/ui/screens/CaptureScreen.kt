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
import com.flowos.app.ui.theme.*

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
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DesignTokens.Spacing.Large),
    ) {
        Spacer(Modifier.height(DesignTokens.Spacing.Large))
        FlowSectionHeader("SYSTEM CAPTURE")
        Spacer(Modifier.height(DesignTokens.Spacing.Small))
        Text(
            "ESTABLISH REALITY",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp
        )
        Spacer(Modifier.height(DesignTokens.Spacing.Huge))

        // ---- TEXT INPUT SURFACE ----
        FlowCard {
            Column {
                TextField(
                    value = typedText,
                    onValueChange = { typedText = it },
                    placeholder = { 
                        Text(
                            "Define your goal, requirement, or intent...", 
                            style = MaterialTheme.typography.bodyLarge, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(DesignTokens.Spacing.Large))
                FlowPrimaryButton(
                    text = "ANALYZE INTENT",
                    onClick = { viewModel.submitTypedText(typedText) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = typedText.isNotBlank()
                )
            }
        }

        Spacer(Modifier.height(DesignTokens.Spacing.Huge))
        FlowSectionHeader("MULTIMODAL SOURCES")
        Spacer(Modifier.height(DesignTokens.Spacing.Medium))

        Row(horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.Medium)) {
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
        Spacer(Modifier.height(DesignTokens.Spacing.Medium))
        FlagshipModeTile(
            icon = Icons.Filled.Description, 
            label = "DOCUMENT", 
            modifier = Modifier.fillMaxWidth(),
            onClick = { docLauncher.launch("*/*") }
        )

        if (state is CaptureUiState.Listening) {
            Spacer(Modifier.height(DesignTokens.Spacing.Huge))
            FlagshipListeningOverlay(onStop = { viewModel.stopVoiceCapture() })
        }

        if (state is CaptureUiState.Error) {
            Spacer(Modifier.height(DesignTokens.Spacing.Large))
            Text(
                (state as CaptureUiState.Error).message, 
                color = MaterialTheme.colorScheme.error, 
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(Modifier.height(80.dp))
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
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(DesignTokens.Shapes.Large),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
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
                    .background(FlowAccent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = FlowAccent, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(DesignTokens.Spacing.Medium))
            Text(
                text = label, 
                style = MaterialTheme.typography.labelMedium, 
                fontWeight = FontWeight.Black, 
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun FlagshipListeningOverlay(onStop: () -> Unit) {
    FlowCard(backgroundColor = FlowAccent.copy(alpha = 0.1f), borderColor = FlowAccent.copy(alpha = 0.4f)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "LISTENING", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Black, 
                color = FlowAccent,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(DesignTokens.Spacing.Small))
            Text(
                "I'm capturing your intent...",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(DesignTokens.Spacing.Large))
            FlowPrimaryButton(text = "STOP", onClick = onStop, modifier = Modifier.fillMaxWidth())
        }
    }
}
