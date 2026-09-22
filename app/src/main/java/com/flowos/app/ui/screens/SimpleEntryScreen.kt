package com.flowos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.*

@Composable
fun SimpleEntryScreen(
    type: String,
    onSave: (String) -> Unit,
    onBack: () -> Unit
) {
    var text by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DesignTokens.Spacing.Large),
    ) {
        Spacer(Modifier.height(DesignTokens.Spacing.Large))
        Row {
            IconButton(onClick = onBack) { Icon(Icons.Filled.Close, null) }
            Spacer(Modifier.width(DesignTokens.Spacing.Small))
            Text(
                text = type.uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
        
        Spacer(Modifier.height(DesignTokens.Spacing.Huge))

        FlowSectionHeader("DEFINE CONTENT")
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("What is the ${type.lowercase()}?") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        )

        Spacer(Modifier.height(DesignTokens.Spacing.Huge))
        FlowPrimaryButton(
            text = "CREATE $type",
            onClick = { onSave(text) },
            modifier = Modifier.fillMaxWidth(),
            enabled = text.isNotBlank()
        )
    }
}
