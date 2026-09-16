package com.flowos.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowos.app.data.local.TaskEntity
import com.flowos.app.domain.model.NextBestAction
import com.flowos.app.ui.theme.*

@Composable
fun PulseCard(
    modifier: Modifier = Modifier,
    accent: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = tween(150),
        label = "pulseCardScale",
    )
    val haptics = LocalHapticFeedback.current

    Card(
        modifier = modifier
            .scale(scale)
            .let { base ->
                if (onClick != null) {
                    base.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                    ) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    }
                } else {
                    base
                }
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (accent) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
        border = if (accent) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        } else null,
    ) {
        Column(Modifier.padding(24.dp), content = content)
    }
}

@Composable
fun NextActionCard(
    action: NextBestAction,
    onStartFocus: () -> Unit,
    onViewFlow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PulseCard(modifier = modifier, accent = true) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
            Spacer(Modifier.width(10.dp))
            FlowSectionHeader("FLOW PULSE", Modifier.weight(1f))
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "CRITICAL",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Black
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = action.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = action.reason,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onStartFocus,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(Icons.Filled.PlayArrow, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("START FOCUS", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun PulseProgress(progress: Float, modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
        strokeCap = StrokeCap.Round
    )
}

// Legacy Mapping
@Composable fun FlowNodeCard(index: Int, task: TaskEntity, isLast: Boolean, modifier: Modifier = Modifier) {}
@Composable fun ContextCard(label: String, value: String, modifier: Modifier = Modifier, accent: Boolean = false) {}
