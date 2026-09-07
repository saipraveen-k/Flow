package com.flowos.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowos.app.action.ActionBundle
import com.flowos.app.action.ActionKind
import com.flowos.app.data.local.TaskEntity
import com.flowos.app.domain.model.EventPreparation
import com.flowos.app.domain.model.NextBestAction
import com.flowos.app.ui.theme.*

/** Radius tokens for the card system (20–28dp per design spec). */
object FlowRadii {
    val Card = 24.dp
    val CardSmall = 20.dp
    val CardLarge = 28.dp
}

private val CardBorder = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))

/**
 * The base surface for the modern card system: thin border, subtle elevation,
 * large padding, 24dp radius. Ordinary content stays neutral; accent is only
 * added where callers pass [accent].
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
        shape = RoundedCornerShape(FlowRadii.Card),
        colors = CardDefaults.cardColors(
            containerColor = if (accent) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (accent) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
        } else {
            CardBorder
        },
    ) {
        Column(Modifier.padding(20.dp), content = content)
    }
}

/** Animated linear progress with accent color. */
@Composable
fun PulseProgress(progress: Float, modifier: Modifier = Modifier, animated: Boolean = true) {
    val target = progress.coerceIn(0f, 1f)
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val animatedProgress by animateFloatAsState(
        targetValue = if (started) target else 0f,
        animationSpec = tween(350),
        label = "pulseProgress",
    )
    val shown = if (animated) animatedProgress else target
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(shown)
                .height(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
        )
    }
}

/** Hero readiness badge: animated percent inside an accent ring. */
@Composable
fun PulseReadinessRing(percent: Int, modifier: Modifier = Modifier) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val scale by animateFloatAsState(
        targetValue = if (started) 1f else 0.6f,
        animationSpec = tween(350),
        label = "readinessReveal",
    )
    Box(
        modifier = modifier
            .size(48.dp)
            .scale(scale)
            .clip(CircleShape)
            .border(3.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.9f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$percent%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/** The AI Pulse / next-best-action card — the signature component of Home. */
@Composable
fun NextActionCard(
    action: NextBestAction,
    onStartFocus: () -> Unit,
    onViewFlow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PulseCard(modifier = modifier, accent = true) {
        FlowSectionHeader("NEXT BEST ACTION")
        Spacer(Modifier.height(10.dp))
        Text(
            text = action.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "WHY?",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
        )
        Text(
            text = action.reason,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onStartFocus,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Icon(Icons.Filled.PlayArrow, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("START FOCUS", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onViewFlow,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            ) {
                Text("VIEW FLOW", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** Calendar event + preparation card for the Plan screen. */
@Composable
fun EventPreparationCard(
    prep: EventPreparation,
    onPrepare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PulseCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Event, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = prep.event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatEventTime(prep.event.beginMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            PulseReadinessRing(percent = prep.readinessPercent, modifier = Modifier.size(48.dp))
        }
        if (prep.hasPreparation) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "${prep.prepTasks.size} preparation task(s) · ${prep.donePrepCount} done",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            PulseProgress(progress = prep.readinessPercent / 100f)
        }
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = onPrepare,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text("PREPARE FOR EVENT", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

/** One node of the Flow dependency graph. */
@Composable
fun FlowNodeCard(
    index: Int,
    task: TaskEntity,
    isLast: Boolean,
    modifier: Modifier = Modifier,
) {
    val priority = com.flowos.app.domain.model.Priority.from(task.priority)
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 90L)
        revealed = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(250),
        label = "nodeReveal",
    )

    Column(modifier = modifier.alpha(alpha)) {
        Card(
            shape = RoundedCornerShape(FlowRadii.CardSmall),
            colors = CardDefaults.cardColors(
                containerColor = if (priority == com.flowos.app.domain.model.Priority.HIGH) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)
                } else {
                    MaterialTheme.colorScheme.surface
                },
            ),
            border = if (priority == com.flowos.app.domain.model.Priority.HIGH) {
                BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            } else {
                CardBorder
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = (index + 1).toString().padStart(2, '0'),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FlowPriorityChip(priority)
                        task.deadlineLabel?.let {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                it,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                if (task.status == com.flowos.app.domain.model.TaskStatus.DONE.name) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        null,
                        tint = Success,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
        if (!isLast) {
            Box(Modifier.padding(start = 24.dp)) {
                Icon(
                    Icons.Filled.ArrowDownward,
                    null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

/** Context-graph node (PROJECT / TASKS / PEOPLE / FILES / DEADLINES / EVENTS). */
@Composable
fun ContextCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
) {
    PulseCard(modifier = modifier, accent = accent) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Action bundle preview + confirmation card. */
@Composable
fun ActionBundleCard(
    bundle: ActionBundle,
    approved: Boolean,
    onApprove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PulseCard(modifier = modifier, accent = !approved) {
        Text(
            text = bundle.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = bundle.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        bundle.items.forEach { item ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = when (item.kind) {
                        ActionKind.CREATE_TASK -> "＋"
                        ActionKind.CREATE_REMINDER -> "⏰"
                        ActionKind.SCHEDULE_EVENT -> "📅"
                        ActionKind.SHARE_TEXT -> "📤"
                        ActionKind.OPEN_FILE -> "📄"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.width(10.dp))
                Text(item.title, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = onApprove,
            enabled = !approved,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = Success.copy(alpha = 0.15f),
                disabledContentColor = Success,
            ),
        ) {
            Text(
                text = if (approved) "✓ APPROVED" else "APPROVE & EXECUTE",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

internal fun formatEventTime(millis: Long): String {
    val formatter = DateTimeFormatterHolder.formatter
    return java.time.Instant.ofEpochMilli(millis)
        .atZone(java.time.ZoneId.systemDefault())
        .format(formatter)
}

private object DateTimeFormatterHolder {
    val formatter: java.time.format.DateTimeFormatter =
        java.time.format.DateTimeFormatter.ofPattern("EEE d MMM · h:mm a", java.util.Locale.US)
}
