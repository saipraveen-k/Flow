package com.flowos.app.ui.components

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowos.app.domain.model.Priority
import com.flowos.app.ui.theme.*

/**
 * Premium FlowOS Card with optional subtle glass effect.
 */
@Composable
fun FlowCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "cardScale"
    )

    Card(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier
            .scale(scale)
            .fillMaxWidth(),
        shape = RoundedCornerShape(DesignTokens.Shapes.Large),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor),
        interactionSource = interactionSource,
    ) {
        Column(Modifier.padding(DesignTokens.Spacing.Large), content = content)
    }
}

/**
 * Modern Glass surface for futuristic feel.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(DesignTokens.Shapes.Medium))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .border(
                1.dp, 
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.2f), Color.Transparent)
                ), 
                RoundedCornerShape(DesignTokens.Shapes.Medium)
            ),
        content = content
    )
}

@Composable
fun FlowSectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 2.sp,
        fontWeight = FontWeight.Black,
        modifier = modifier.padding(vertical = DesignTokens.Spacing.Medium),
    )
}

@Composable
fun FlowPriorityChip(priority: Priority, modifier: Modifier = Modifier) {
    val pair = when (priority) {
        Priority.HIGH -> Error to "CRITICAL"
        Priority.MEDIUM -> Warning to "ATTENTION"
        Priority.LOW -> Success to "VERIFIED"
    }
    val color = pair.first
    val label = pair.second
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(DesignTokens.Shapes.Small),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = DesignTokens.Spacing.Small, vertical = DesignTokens.Spacing.Tiny),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
            Spacer(Modifier.width(DesignTokens.Spacing.Small))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun FlowPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    containerColor: Color = FlowAccent,
    contentColor: Color = Color.Black
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "btnScale")
    val haptics = LocalHapticFeedback.current

    Button(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier.height(64.dp).scale(scale),
        shape = RoundedCornerShape(DesignTokens.Shapes.Large),
        enabled = enabled,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(DesignTokens.Spacing.Medium))
            }
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun FlowSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1f, label = "btnScale")

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(60.dp).scale(scale),
        shape = RoundedCornerShape(DesignTokens.Shapes.Large),
        enabled = enabled,
        interactionSource = interactionSource,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun FlowProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = FlowAccent
) {
    Column(modifier = modifier) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = color,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun FlowLoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = FlowAccent, strokeWidth = 4.dp)
    }
}

@Composable
fun FlowEmptyState(
    title: String,
    description: String,
    icon: ImageVector = Icons.Default.Info,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(DesignTokens.Spacing.Section),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.height(DesignTokens.Spacing.Large))
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        Spacer(Modifier.height(DesignTokens.Spacing.Small))
        Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

// Legacy Mapping
@Composable fun TaskRow(title: String, deadlineLabel: String?, priority: Priority, checked: Boolean, onCheck: (() -> Unit)?, modifier: Modifier = Modifier) = 
    FlowCard(modifier = modifier, onClick = onCheck) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = checked, onClick = onCheck)
            Spacer(Modifier.width(DesignTokens.Spacing.Medium))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                if (deadlineLabel != null) {
                    Text(deadlineLabel, style = MaterialTheme.typography.labelSmall, color = FlowAccent)
                }
            }
            FlowPriorityChip(priority)
        }
    }

@Composable fun FlowTaskCard(title: String, deadlineLabel: String?, priority: Priority, checked: Boolean, onCheck: (() -> Unit)?, modifier: Modifier = Modifier) =
    TaskRow(title, deadlineLabel, priority, checked, onCheck, modifier)

@Composable fun FlowErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(DesignTokens.Spacing.Section),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Snag detected", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(DesignTokens.Spacing.Small))
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(DesignTokens.Spacing.Large))
        FlowSecondaryButton(text = "TRY AGAIN", onClick = onRetry)
    }
}

@Composable fun FlowTimelineStep(number: String, title: String, detail: String?, priority: Priority, isLast: Boolean = false, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(FlowAccent.copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, FlowAccent.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = number, style = MaterialTheme.typography.labelLarge, color = FlowAccent, fontWeight = FontWeight.Black)
            }
            if (!isLast) {
                Box(modifier = Modifier.width(2.dp).weight(1f).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)))
            }
        }
        Spacer(Modifier.width(DesignTokens.Spacing.Medium))
        Column(modifier = Modifier.padding(bottom = DesignTokens.Spacing.Large)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (detail != null) {
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable fun SectionLabel(text: String, modifier: Modifier = Modifier) = FlowSectionHeader(text, modifier)
@Composable fun PriorityChip(priority: Priority, modifier: Modifier = Modifier) = FlowPriorityChip(priority, modifier)
