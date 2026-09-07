package com.flowos.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.R
import com.flowos.app.ui.CaptureUiState
import com.flowos.app.ui.CaptureViewModel
import com.flowos.app.ui.components.*
import java.io.File

@Composable
fun CaptureScreen(
    viewModel: CaptureViewModel,
    onReadyToProcess: () -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var typedText by remember { mutableStateOf("") }
    var showTypedInput by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) pendingCameraUri?.let(viewModel::onImageCaptured)
    }
    
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            pendingCameraUri?.let { cameraLauncher.launch(it) }
        } else {
            viewModel.setError("Camera permission is required to capture photos.")
        }
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri -> uri?.let(viewModel::onImageCaptured) }
    
    val documentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri -> uri?.let(viewModel::onDocumentSelected) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.startVoiceCapture()
        } else {
            viewModel.setError("Microphone permission is required for voice capture.")
        }
    }

    fun startVoiceWithPermission() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) viewModel.startVoiceCapture() else micPermissionLauncher.launch(
            Manifest.permission.RECORD_AUDIO,
        )
    }

    LaunchedEffect(uiState) {
        if (uiState is CaptureUiState.Ready) onReadyToProcess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Text("CAPTURE", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(6.dp))
        Text(
            "Give FlowOS something to understand.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))

        FlowCaptureOption(
            title = "IMAGE",
            subtitle = "Capture or select an image",
            icon = Icons.Filled.CameraAlt,
            onClick = {
                val uri = newCaptureUri(context)
                pendingCameraUri = uri
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            },
        )
        Spacer(Modifier.height(16.dp))
        FlowCaptureOption(
            title = "VOICE",
            subtitle = if (uiState is CaptureUiState.Listening) "Listening..." else "Tell FlowOS what you need",
            icon = Icons.Filled.Mic,
            onClick = {
                if (uiState is CaptureUiState.Listening) {
                    viewModel.stopVoiceCapture()
                } else {
                    startVoiceWithPermission()
                }
            },
        )
        Spacer(Modifier.height(16.dp))
        FlowCaptureOption(
            title = "DOCUMENT",
            subtitle = "Import text or document content",
            icon = Icons.Filled.Description,
            onClick = { documentLauncher.launch("*/*") },
        )
        
        Spacer(Modifier.height(32.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically, 
            modifier = Modifier.clickable { showTypedInput = !showTypedInput }
        ) {
            Icon(Icons.Filled.Edit, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text("QUICK TEXT", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }

        AnimatedVisibility(visible = showTypedInput) {
            Column {
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = typedText,
                    onValueChange = { typedText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(stringResource(id = R.string.demo_capture_text))
                    },
                    minLines = 3,
                    shape = RoundedCornerShape(20.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                Spacer(Modifier.height(16.dp))
                FlowPrimaryButton(
                    text = "ANALYZE",
                    onClick = { viewModel.submitTypedText(typedText) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = typedText.isNotBlank()
                )
            }
        }

        // Status / Error Banners
        when (val state = uiState) {
            is CaptureUiState.Listening -> {
                Spacer(Modifier.height(24.dp))
                StatusBanner("Listening… speak now", Icons.Filled.Mic)
            }
            is CaptureUiState.Processing -> {
                Spacer(Modifier.height(24.dp))
                StatusBanner("Analyzing locally...", Icons.Filled.ArrowForward)
            }
            is CaptureUiState.Error -> {
                Spacer(Modifier.height(24.dp))
                Text(
                    state.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            else -> Unit
        }
        
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun StatusBanner(text: String, icon: ImageVector) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private fun newCaptureUri(context: Context): Uri {
    val dir = File(context.cacheDir, "captures").apply { mkdirs() }
    val file = File(dir, "camera_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
