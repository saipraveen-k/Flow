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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowos.app.domain.model.*
import com.flowos.app.ui.components.*
import com.flowos.app.util.TimeParser

@Composable
fun UnderstandingScreen(
    analysis: AIAnalysisResult?,
    outcome: Outcome?,
    onBuildWorkflow: () -> Unit,
    onEdit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        FlowSectionHeader("OUTCOME COMPILER")
        Spacer(Modifier.height(8.dp))
        Text("I UNDERSTOOD", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
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

        // ---- OUTCOME SUMMARY ---------------------------------------------
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(analysis.summary.uppercase(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InfoItem(label = "DUE", value = outcome?.deadlineEpochMillis?.let { TimeParser.humanLabel(it) } ?: "No deadline")
                    Spacer(Modifier.width(32.dp))
                    InfoItem(label = "CONTEXT", value = outcome?.hub?.name ?: "Professional")
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        FlowSectionHeader("PROPOSED PLAN")
        Spacer(Modifier.height(12.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            analysis.tasks.forEach { task ->
                PlanTaskItem(task)
            }
        }

        Spacer(Modifier.height(32.dp))
        FlowSectionHeader("DEPENDENCY GRAPH")
        Spacer(Modifier.height(12.dp))
        
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(24.dp)) {
                analysis.tasks.forEachIndexed { index, task ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                        Spacer(Modifier.width(12.dp))
                        Text(task.title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                    if (index != analysis.tasks.lastIndex) {
                        Box(modifier = Modifier.padding(start = 3.dp).width(1.dp).height(16.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)))
                    }
                }
            }
        }

        Spacer(Modifier.height(40.dp))
        FlowPrimaryButton(
            text = "CREATE OUTCOME",
            onClick = onBuildWorkflow,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        FlowSecondaryButton(
            text = "EDIT DETAILS",
            onClick = onEdit,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun PlanTaskItem(task: ExtractedTask) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text("${task.estimatedDurationMinutes} min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (task.priority == Priority.HIGH) {
                StatusBadge("CRITICAL", MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun StatusBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Black
        )
    }
}
