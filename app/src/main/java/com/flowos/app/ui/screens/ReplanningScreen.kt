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
import com.flowos.app.ui.theme.FlowAccent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * REPLANNING: Flagship Intelligence Recovery Surface.
 * Premium BEFORE vs AFTER transformation with technical reasoning.
 */
@Composable
fun ReplanningScreen(
    proposal: AdaptiveReplanner.ReplanProposal,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    var showAfter by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070707))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(32.dp))
        FlowSectionHeader("ADAPTIVE RECOVERY")
        Spacer(Modifier.height(12.dp))
        Text(
            "YOUR PLAN ADAPTED", 
            style = MaterialTheme.typography.headlineMedium, 
            fontWeight = FontWeight.Black, 
            color = Color.White,
            letterSpacing = (-1).sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Reality diverged from your plan. FlowOS has optimized your remaining work.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
        
        Spacer(Modifier.height(40.dp))
        
        // ---- WHY PANEL: Intelligence explanation ------------------------
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(28.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(20.dp))
                Column {
                    Text("FRICTION DETECTED", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error, letterSpacing = 2.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(proposal.reason, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(Modifier.height(40.dp))
        
        // ---- TRANSFORMATION TOGGLE: Premium BEFORE/AFTER Switch ----------
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
                color = Color.Gray
            )
            Surface(
                color = Color(0xFF161616),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("BEFORE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = if (!showAfter) FlowAccent else Color.Gray)
                    Switch(checked = showAfter, onCheckedChange = { showAfter = it }, modifier = Modifier.scale(0.7f).padding(horizontal = 8.dp))
                    Text("AFTER", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = if (showAfter) FlowAccent else Color.Gray)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ---- COMPARISON TIMELINE -----------------------------------------
        AnimatedContent(
            targetState = showAfter,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "timelineMorph"
        ) { isAfter ->
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101010)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(28.dp)) {
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

        Spacer(Modifier.height(40.dp))
        
        // ---- RATIONALE: Strategic Summary --------------------------------
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "WHY THIS PLAN?",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = FlowAccent,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                RationaleBadge("PROTECTS DEADLINE")
                RationaleBadge("PRESERVES FLOW")
            }
        }

        Spacer(Modifier.height(48.dp))
        FlowPrimaryButton(text = "APPLY NEW PLAN", onClick = onAccept, modifier = Modifier.fillMaxWidth(), icon = Icons.Filled.Bolt)
        Spacer(Modifier.height(12.dp))
        FlowSecondaryButton(text = "KEEP ORIGINAL", onClick = onReject, modifier = Modifier.fillMaxWidth())
        
        Spacer(Modifier.height(60.dp))
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
                    .background(if (isShifted) FlowAccent else Color.Gray.copy(alpha = 0.4f))
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .weight(1f)
                        .background(Color.Gray.copy(alpha = 0.2f))
                )
            }
        }
        Spacer(Modifier.width(20.dp))
        Column(modifier = Modifier.padding(bottom = 24.dp).weight(1f)) {
            Text(
                text = title, 
                style = MaterialTheme.typography.bodyLarge, 
                fontWeight = if (isShifted) FontWeight.Black else FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = time, 
                style = MaterialTheme.typography.labelSmall, 
                color = if (isShifted) FlowAccent else Color.Gray, 
                fontWeight = FontWeight.Black
            )
        }
        if (isShifted) {
            Surface(
                color = FlowAccent.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
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
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatTime(millis: Long): String {
    val instant = Instant.ofEpochMilli(millis)
    val dateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
    return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
}
