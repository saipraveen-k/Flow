package com.flowos.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowos.app.ui.ContextGraphViewModel
import com.flowos.app.ui.components.ContextCard
import com.flowos.app.ui.components.FlowSectionHeader
import com.flowos.app.ui.components.PulseCard
import com.flowos.app.ui.components.PulseProgress
import kotlin.math.cos
import kotlin.math.sin

/**
 * CONTEXT: a clean knowledge-graph visualization. The active project sits at
 * the center; category satellites (TASKS / PEOPLE / FILES / DEADLINES / EVENTS)
 * connect to it. Custom Canvas layout — no graph library, fully stable.
 */
@Composable
fun ContextScreen(viewModel: ContextGraphViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Text("CONTEXT", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(4.dp))
        Text(
            "Your work knowledge graph.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))

        if (state.projectName == null) {
            Icon(
                Icons.Filled.AccountTree,
                null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "No project context yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                "Capture work and FlowOS will connect tasks, people, files and events into one graph.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(40.dp))
            return@Column
        }

        // ---- Graph canvas ---------------------------------------------------
        KnowledgeGraph(
            projectName = state.projectName ?: "",
            completion = state.completionPercentage,
            taskCount = state.openTasks.size,
            peopleCount = state.people.size,
            fileCount = state.files.size,
            deadlineCount = state.deadlines.size,
            eventCount = state.events.size,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
        )

        Spacer(Modifier.height(24.dp))

        // ---- Project summary -------------------------------------------------
        PulseCard(accent = true) {
            Text(state.projectName ?: "", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            PulseProgress(progress = state.completionPercentage / 100f)
            Spacer(Modifier.height(6.dp))
            Text(
                "${state.completionPercentage}% complete · ${state.openTasks.size} open · ${state.completedCount} done",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            state.nextBestAction?.let { action ->
                Spacer(Modifier.height(10.dp))
                Text(
                    "NEXT: ${action.title}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // ---- Connected entities -----------------------------------------------
        FlowSectionHeader("CONNECTED ENTITIES")
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ContextCard("TASKS", state.openTasks.size.toString(), Modifier.weight(1f))
            ContextCard("PEOPLE", state.people.joinToString(", ").ifEmpty { "—" }, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ContextCard("DEADLINES", state.deadlines.size.toString(), Modifier.weight(1f))
            ContextCard("EVENTS", state.events.size.toString(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        ContextCard(
            "FILES IN PLAY",
            state.files.joinToString(", ").ifEmpty { "No file-bearing tasks" },
            modifier = Modifier.fillMaxWidth(),
        )

        if (state.captures.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            FlowSectionHeader("SOURCE CAPTURES")
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.captures.take(4).forEach { capture ->
                    PulseCard {
                        Text(
                            capture.summary ?: capture.rawText.take(80),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${capture.sourceType} · ${capture.detectedPeople.ifEmpty { "no people" }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

/** Radial graph: project node at center, five category satellites around it. */
@Composable
private fun KnowledgeGraph(
    projectName: String,
    completion: Int,
    taskCount: Int,
    peopleCount: Int,
    fileCount: Int,
    deadlineCount: Int,
    eventCount: Int,
    modifier: Modifier = Modifier,
) {
    val accent = MaterialTheme.colorScheme.primary
    val lineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val nodeColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant

    data class Satellite(val label: String, val count: Int, val angleDegrees: Double)
    val satellites = listOf(
        Satellite("TASKS", taskCount, -90.0),
        Satellite("PEOPLE", peopleCount, -18.0),
        Satellite("FILES", fileCount, 54.0),
        Satellite("DEADLINES", deadlineCount, 126.0),
        Satellite("EVENTS", eventCount, 198.0),
    )
    val satelliteRadiusDp = 104f

    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            satellites.forEach { satellite ->
                val angle = Math.toRadians(satellite.angleDegrees)
                drawLine(
                    color = if (satellite.count > 0) accent.copy(alpha = 0.5f) else lineColor,
                    start = center,
                    end = Offset(
                        center.x + (size.minDimension * 0.36f * cos(angle)).toFloat(),
                        center.y + (size.minDimension * 0.36f * sin(angle)).toFloat(),
                    ),
                    strokeWidth = 2f,
                    cap = StrokeCap.Round,
                )
            }
        }
        satellites.forEach { satellite ->
            val angle = Math.toRadians(satellite.angleDegrees)
            SatelliteNode(
                label = satellite.label,
                count = satellite.count,
                color = nodeColor,
                textColor = if (satellite.count > 0) textColor else mutedColor,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(
                        x = (satelliteRadiusDp * cos(angle)).dp,
                        y = (satelliteRadiusDp * sin(angle)).dp,
                    ),
            )
        }
        // Center project node.
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "$completion%",
                    style = MaterialTheme.typography.titleLarge,
                    color = accent,
                    fontWeight = FontWeight.Black,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                projectName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SatelliteNode(
    label: String,
    count: Int,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontSize = 9.sp,
            letterSpacing = 0.8.sp,
        )
    }
}
