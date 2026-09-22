package com.flowos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.ui.ActivityViewModel
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ActivityScreen(
    viewModel: ActivityViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DesignTokens.Spacing.Large),
    ) {
        Spacer(Modifier.height(DesignTokens.Spacing.Large))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            Text("ACTIVITY", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(DesignTokens.Spacing.Tiny))
        Text(
            "Audit trail of your intelligent workflows.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(DesignTokens.Spacing.Huge))

        if (uiState.activityEvents.isEmpty()) {
            FlowEmptyState(
                title = "No recent activity",
                description = "Your actions and workflow generations will appear here.",
                icon = Icons.Filled.History
            )
        } else {
            FlowSectionHeader("SYSTEM TIMELINE")
            Spacer(Modifier.height(DesignTokens.Spacing.Medium))
            
            uiState.activityEvents.forEachIndexed { index, event ->
                ActivityTimelineItem(
                    title = event.title,
                    detail = event.detail,
                    time = formatEventTime(event.createdAt),
                    isLast = index == uiState.activityEvents.size - 1
                )
            }
        }
        
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun ActivityTimelineItem(
    title: String,
    detail: String?,
    time: String,
    isLast: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(FlowAccent)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                )
            }
        }
        Spacer(Modifier.width(DesignTokens.Spacing.Large))
        Column(modifier = Modifier.padding(bottom = DesignTokens.Spacing.Large)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text(time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (detail != null) {
                Spacer(Modifier.height(DesignTokens.Spacing.Tiny))
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun formatEventTime(millis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(formatter)
}
