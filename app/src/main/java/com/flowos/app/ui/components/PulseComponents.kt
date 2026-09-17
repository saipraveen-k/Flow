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

/**
 * Flagship Card System: Deep black surfaces, kinetic yellow highlights, 24-32dp radii.
 */
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
                FlowAccent.copy(alpha = 0.05f)
            } else {
                Color(0xFF101010)
            },
        ),
        border = if (accent) {
            BorderStroke(1.dp, FlowAccent.copy(alpha = 0.3f))
        } else {
            BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        },
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
                    .background(FlowAccent, CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            FlowSectionHeader("NEXT BEST ACTION", Modifier.weight(1f))
            Surface(
                color = FlowAccent,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "CRITICAL",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black,
                    fontWeight = FontWeight.Black
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = action.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Info, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = action.reason,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
            )
        }
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FlowPrimaryButton(
                text = "START FOCUS",
                onClick = onStartFocus,
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.PlayArrow
            )
            IconButton(
                onClick = onViewFlow,
                modifier = Modifier.background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)).size(64.dp)
            ) {
                Icon(Icons.Filled.AccountTree, null, tint = FlowAccent)
            }
        }
    }
}

@Composable
fun PulseProgress(progress: Float, modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
        color = FlowAccent,
        trackColor = Color.White.copy(alpha = 0.05f),
        strokeCap = StrokeCap.Round
    )
}

// Legacy Mapping
@Composable fun FlowNodeCard(index: Int, task: TaskEntity, isLast: Boolean, modifier: Modifier = Modifier) {}
@Composable fun ContextCard(label: String, value: String, modifier: Modifier = Modifier, accent: Boolean = false) {}
