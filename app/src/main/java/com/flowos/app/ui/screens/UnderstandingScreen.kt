package com.flowos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowos.app.domain.model.AIAnalysisResult
import com.flowos.app.domain.model.ExtractedTask
import com.flowos.app.domain.model.Priority
import com.flowos.app.ui.components.*

@Composable
fun UnderstandingScreen(
    analysis: AIAnalysisResult?,
    processingLabel: String,
    onBuildWorkflow: () -> Unit,
    onEdit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Text("UNDERSTANDING", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(4.dp))
        Text(
            text = analysis?.let { "I found ${it.tasks.size} actions" } ?: "Nothing to show",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(32.dp))

        if (analysis == null) {
            FlowEmptyState(
                title = "No insights found",
                description = "FlowOS couldn't extract tasks from this capture. Try rephrasing or use a clearer image."
            )
            Spacer(Modifier.height(24.dp))
            FlowSecondaryButton(text = "BACK TO CAPTURE", onClick = onEdit, modifier = Modifier.fillMaxWidth())
            return@Column
        }

        // Summary Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FlowSectionHeader("SUMMARY", Modifier.weight(1f))
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "SCORE ${(analysis.confidence * 100).toInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(analysis.summary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                analysis.detectedIntent?.let { intent ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "INTENT: ${intent.label} · ${(intent.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    )
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FlowPriorityChip(analysis.priority)
                    analysis.project?.let { 
                        FlowDeadlineChip(label = it)
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        FlowSectionHeader("EXTRACTED TASKS")
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            analysis.tasks.forEach { task ->
                ActionCard(task = task)
            }
        }

        Spacer(Modifier.height(32.dp))
        FlowSectionHeader("DETECTED CONTEXT")
        Spacer(Modifier.height(12.dp))
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ContextItem(
                    icon = Icons.Filled.Person,
                    label = "People",
                    value = analysis.people.joinToString(", ").ifEmpty { "None" }
                )
                ContextItem(
                    icon = Icons.Filled.CalendarMonth,
                    label = "Project",
                    value = analysis.project ?: "Unassigned"
                )
                ContextItem(
                    icon = Icons.Filled.AccessTime,
                    label = "Deadlines",
                    value = analysis.deadlines.joinToString(", ") { it.label }.ifEmpty { "None detected" }
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            processingLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            letterSpacing = 0.5.sp
        )

        Spacer(Modifier.height(32.dp))
        FlowPrimaryButton(
            text = "BUILD WORKFLOW",
            onClick = onBuildWorkflow,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        FlowSecondaryButton(
            text = "EDIT CAPTURE",
            onClick = onEdit,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun ActionCard(task: ExtractedTask) {
    val tag = when {
        task.requiresSharing -> "SHARE"
        task.deadlineLabel != null -> "REMAIND"
        else -> "TASK"
    }
    val tagIcon = when (tag) {
        "SHARE" -> Icons.Filled.Send
        "REMAIND" -> Icons.Filled.AccessTime
        else -> Icons.Filled.Check
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(tagIcon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(tag, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { /* Edit Task logic would go here in full implementation */ }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Edit, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (task.deadlineLabel != null) {
                Spacer(Modifier.height(6.dp))
                Text("Due ${task.deadlineLabel}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun ContextItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}
