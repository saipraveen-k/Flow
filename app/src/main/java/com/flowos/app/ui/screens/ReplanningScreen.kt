package com.flowos.app.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowos.app.planner.AdaptiveReplanner
import com.flowos.app.ui.components.*
import com.flowos.app.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * REPLANNING: Flagship Intelligence Recovery Surface.
 */
@Composable
fun ReplanningScreen(
    proposal: AdaptiveReplanner.ReplanProposal,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    var showAfter by remember { mutableStateOf(true) } // Default to 'After' to show the proposal

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DesignTokens.Spacing.Large),
    ) {
        Spacer(Modifier.height(DesignTokens.Spacing.Large))
        FlowSectionHeader("ADAPTIVE RECOVERY")
        Spacer(Modifier.height(DesignTokens.Spacing.Small))
        Text(
            "YOUR PLAN ADAPTED", 
            style = MaterialTheme.typography.headlineMedium, 
            fontWeight = FontWeight.Black, 
            letterSpacing = (-1).sp
        )
        Spacer(Modifier.height(DesignTokens.Spacing.Small))
        Text(
            text = "Reality diverged from your plan. FlowOS has optimized your remaining work.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(Modifier.height(DesignTokens.Spacing.Huge))
        
        // ---- WHY PANEL ----
        FlowCard(backgroundColor = MaterialTheme.colorScheme.error.copy(alpha = 0.05f), borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(DesignTokens.Spacing.Large))
                Column {
                    Text("FRICTION DETECTED", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error, letterSpacing = 2.sp)
                    Spacer(Modifier.height(DesignTokens.Spacing.Tiny))
                    Text(proposal.reason, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(DesignTokens.Spacing.Huge))
        
        // ---- TRANSFORMATION TOGGLE ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "TIMELINE TRANSFORMATION", 
                style = MaterialTheme.typography.labelSmall, 
                fontWeight = FontWeight.Black, 
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(DesignTokens.Shapes.Medium)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("BEFORE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = if (!showAfter) FlowAccent else MaterialTheme.colorScheme.onSurfaceVariant)
                    Switch(checked = showAfter, onCheckedChange = { showAfter = it }, modifier = Modifier.scale(0.7f).padding(horizontal = 8.dp))
                    Text("AFTER", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = if (showAfter) FlowAccent else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(DesignTokens.Spacing.Large))

        // ---- COMPARISON TIMELINE ----
        AnimatedContent(
            targetState = showAfter,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "timelineMorph"
        ) { isAfter ->
            FlowCard {
                Column {
                    val items = if (isAfter) proposal.afterItems else proposal.beforeItems
                    items.forEachIndexed { index, item ->
                        val shifted = if (isAfter) {
                            val beforeItem = proposal.beforeItems.getOrNull(index)
                            beforeItem != null && (item.startMillis != beforeItem.startMillis || item.endMillis != beforeItem.endMillis)
                        } else false
                        
                        AdaptiveTimelineRow(
                            title = item.title,
                            time = "${formatTime(item.startMillis)} — ${formatTime(item.endMillis)}",
                            isShifted = shifted,
                            isLast = index == items.lastIndex
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(DesignTokens.Spacing.Huge))
        
        // ---- RATIONALE ----
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "SYSTEM RATIONALE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = FlowAccent,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(DesignTokens.Spacing.Medium))
            Row(horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.Medium)) {
                RationaleBadge("PROTECTS DEADLINE")
                RationaleBadge("PRESERVES FLOW")
            }
        }

        Spacer(Modifier.height(DesignTokens.Spacing.Huge))
        FlowPrimaryButton(text = "APPLY ADAPTATION", onClick = onAccept, modifier = Modifier.fillMaxWidth(), icon = Icons.Filled.Bolt)
        Spacer(Modifier.height(DesignTokens.Spacing.Medium))
        FlowSecondaryButton(text = "KEEP ORIGINAL PLAN", onClick = onReject, modifier = Modifier.fillMaxWidth())
        
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun AdaptiveTimelineRow(title: String, time: String, isShifted: Boolean, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isShifted) FlowAccent else MaterialTheme.colorScheme.outline)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                )
            }
        }
        Spacer(Modifier.width(DesignTokens.Spacing.Large))
        Column(modifier = Modifier.padding(bottom = DesignTokens.Spacing.Large).weight(1f)) {
            Text(
                text = title, 
                style = MaterialTheme.typography.bodyLarge, 
                fontWeight = if (isShifted) FontWeight.Black else FontWeight.Bold
            )
            Text(
                text = time, 
                style = MaterialTheme.typography.labelSmall, 
                color = if (isShifted) FlowAccent else MaterialTheme.colorScheme.onSurfaceVariant, 
                fontWeight = FontWeight.Black
            )
        }
        if (isShifted) {
            Surface(
                color = FlowAccent.copy(alpha = 0.1f),
                shape = RoundedCornerShape(DesignTokens.Shapes.Small)
            ) {
                Text(
                    text = "SHIFTED", 
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall, 
                    color = FlowAccent, 
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun RationaleBadge(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(DesignTokens.Shapes.Medium),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatTime(millis: Long): String {
    val instant = Instant.ofEpochMilli(millis)
    val dateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
    return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
}
