package com.flowos.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowos.app.domain.model.WorkflowPlan
import com.flowos.app.ui.components.*

@Composable
fun WorkflowScreen(
    plan: WorkflowPlan?,
    dependencyCount: Int,
    onExecute: () -> Unit,
    onEdit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Text("YOUR WORKFLOW", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(4.dp))
        Text(
            text = plan?.title ?: "Untitled Workflow",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(32.dp))

        if (plan == null || plan.steps.isEmpty()) {
            FlowEmptyState(
                title = "No workflow generated",
                description = "FlowOS needs a capture to build a productivity plan."
            )
            Spacer(Modifier.height(24.dp))
            FlowPrimaryButton(text = "START CAPTURE", onClick = onEdit, modifier = Modifier.fillMaxWidth())
            return@Column
        }

        // Project Header with Progress
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(24.dp)) {
                FlowProgress(progress = 0f) // Initial progress for a new workflow
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FlowSectionHeader(plan.title, Modifier.weight(1f))
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        FlowSectionHeader("TIMELINE")
        Spacer(Modifier.height(16.dp))

        Column(modifier = Modifier.padding(start = 8.dp)) {
            plan.steps.forEachIndexed { index, step ->
                FlowTimelineStep(
                    number = (index + 1).toString().padStart(2, '0'),
                    title = step.title,
                    detail = step.deadlineLabel,
                    priority = step.priority,
                    isLast = index == plan.steps.size - 1
                )
            }
        }

        Spacer(Modifier.height(32.dp))
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${plan.steps.size} steps • ${plan.deadlineCount} deadlines",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(Modifier.height(32.dp))
        FlowPrimaryButton(
            text = "EXECUTE WORKFLOW",
            onClick = onExecute,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        FlowSecondaryButton(
            text = "REVISE PLAN",
            onClick = onEdit,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(40.dp))
    }
}
