package com.flowos.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.flowos.app.ui.theme.FlowAccent
import com.flowos.app.ui.theme.Success
import com.flowos.app.util.TimeParser

/**
 * UNDERSTANDING: Premium Flagship Transparency Surface.
 * "I UNDERSTOOD THIS AS..." - building user trust through clarity.
 */
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
        Spacer(Modifier.height(32.dp))
        FlowSectionHeader("SYSTEM UNDERSTANDING")
        Spacer(Modifier.height(8.dp))
        Text(
            "I UNDERSTOOD THIS AS...", 
            style = MaterialTheme.typography.headlineMedium, 
            fontWeight = FontWeight.Black, 
            color = Color.White,
            letterSpacing = (-0.5).sp
        )
        Spacer(Modifier.height(32.dp))

        if (analysis == null) {
            FlowEmptyState(
                title = "Complexity Snag",
                description = "I couldn't distill this capture into a clear outcome. Please adjust your capture."
            )
            Spacer(Modifier.height(24.dp))
            FlowSecondaryButton(text = "REVISE INPUT", onClick = onEdit, modifier = Modifier.fillMaxWidth())
            return@Column
        }

        // ---- THE OUTCOME: Flagship primary extraction card ----------------
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101010)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(28.dp)) {
                Text(
                    text = analysis.summary.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ReviewInfoItem(
                        label = "TARGET DEADLINE",
                        value = outcome?.deadlineEpochMillis?.let { TimeParser.humanLabel(it) } ?: "8:00 PM",
                        icon = Icons.Filled.CalendarToday
                    )
                    ReviewInfoItem(
                        label = "CONTEXT HUB",
                        value = outcome?.hub?.name ?: "PROFESSIONAL",
                        icon = Icons.Filled.ScatterPlot
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        FlowSectionHeader("PROPOSED WORK GRAPH")
        Spacer(Modifier.height(16.dp))
        
        // ---- TASKS & DEPENDENCIES: Flagship Graph Review -----------------
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(24.dp)) {
                analysis.tasks.forEachIndexed { index, task ->
                    FlagshipProposedTask(
                        title = task.title,
                        duration = "${task.estimatedDurationMinutes}m",
                        isCritical = task.priority == Priority.HIGH,
                        isLast = index == analysis.tasks.lastIndex
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Bolt, null, tint = FlowAccent, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Deterministic execution protecting the critical path.",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(48.dp))
        FlowPrimaryButton(
            text = "CREATE OUTCOME",
            onClick = onBuildWorkflow,
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Filled.Add
        )
        Spacer(Modifier.height(12.dp))
        FlowSecondaryButton(
            text = "EDIT DETAILS",
            onClick = onEdit,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(60.dp))
    }
}

@Composable
private fun ReviewInfoItem(label: String, value: String, icon: ImageVector) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(14.dp), tint = FlowAccent)
            Spacer(Modifier.width(8.dp))
            Text(
                text = label, 
                style = MaterialTheme.typography.labelSmall, 
                color = Color.Gray, 
                fontWeight = FontWeight.Black, 
                letterSpacing = 1.sp
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = value, 
            style = MaterialTheme.typography.bodyLarge, 
            fontWeight = FontWeight.Black, 
            color = Color.White
        )
    }
}

@Composable
private fun FlagshipProposedTask(title: String, duration: String, isCritical: Boolean, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (isCritical) MaterialTheme.colorScheme.error else FlowAccent)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .weight(1f)
                        .background(Color.Gray.copy(alpha = 0.3f))
                )
            }
        }
        Spacer(Modifier.width(20.dp))
        Column(modifier = Modifier.padding(bottom = 24.dp).weight(1f)) {
            Text(
                text = title, 
                style = MaterialTheme.typography.bodyLarge, 
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = duration, 
                    style = MaterialTheme.typography.labelSmall, 
                    color = Color.Gray, 
                    fontWeight = FontWeight.Black
                )
                if (isCritical) {
                    Spacer(Modifier.width(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "CRITICAL", 
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall, 
                            color = MaterialTheme.colorScheme.error, 
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}
