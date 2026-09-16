package com.flowos.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowos.app.ui.components.*

@Composable
fun ReplanningScreen(
    reason: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    var showAfter by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        FlowSectionHeader("ADAPTIVE REPLANNING")
        Spacer(Modifier.height(12.dp))
        Text("REALITY CHANGED", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "FlowOS detected friction in your execution.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(Modifier.height(32.dp))
        
        // ---- WHY CARD ----------------------------------------------------
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.05f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Error, null, tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("FRICTION CAUSE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error)
                    Text(reason, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        
        // ---- BEFORE / AFTER TOGGLE ---------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("RECOVERY PLAN", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("BEFORE", style = MaterialTheme.typography.labelSmall, color = if (!showAfter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                Switch(checked = showAfter, onCheckedChange = { showAfter = it }, modifier = Modifier.scale(0.7f))
                Text("AFTER", style = MaterialTheme.typography.labelSmall, color = if (showAfter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(16.dp))

        // ---- PLAN COMPARISON ---------------------------------------------
        AnimatedContent(
            targetState = showAfter,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "planTransition"
        ) { isAfter ->
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(24.dp)) {
                    if (isAfter) {
                        PlanItem("Architecture", "6:00", "7:17", true)
                        PlanItem("Testing", "7:15", "7:37", true)
                        PlanItem("Demo", "8:00", "8:00", false)
                    } else {
                        PlanItem("Architecture", "6:00", "6:45", false)
                        PlanItem("Testing", "6:45", "7:15", false)
                        PlanItem("Demo", "7:15", "8:00", false)
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        Text(
            text = "FlowOS protected the 8 PM outcome by reallocating remaining available time.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))
        FlowPrimaryButton(text = "ACCEPT RECOVERY PLAN", onClick = onAccept, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        FlowSecondaryButton(text = "KEEP ORIGINAL PLAN", onClick = onReject, modifier = Modifier.fillMaxWidth())
        
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun PlanItem(title: String, start: String, end: String, shifted: Boolean) {
    Row(
        modifier = Modifier.padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("$start — $end", style = MaterialTheme.typography.bodySmall, color = if (shifted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (shifted) FontWeight.Bold else FontWeight.Normal)
        }
        if (shifted) {
            StatusBadge("SHIFTED", MaterialTheme.colorScheme.primary)
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
